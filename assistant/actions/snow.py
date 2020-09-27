import asyncio
import json
import logging
import os
import signal
from typing import Dict, Text, Any
from aiohttp import ClientSession, BasicAuth
from actions.vault import Vault
from actions.util import anonymous_profile, priorities, states

logger = logging.getLogger(__name__)

class SnowAPI:
    """class to connect to the ServiceNow API"""

    def __init__(self):
        vault = Vault()
        secret = vault.load_secret("snow_service")
        self.auth = BasicAuth(
            login=secret.get("user"),
            password=secret.get("pwd"),
            encoding="utf-8"
        )
        snow_instance = secret.get("instance")
        self.base_api_url = f"https://{snow_instance}/api/now"        
        self._session = None

        # Hook into the os's shutdown signal to
        # asynchronously close the client session.
        loop = asyncio.get_event_loop()
        task = loop.create_task(self.close_session())
        loop.add_signal_handler(signal.SIGTERM, task)
        self._loop = loop

    async def open_session(self) -> ClientSession:  
        """Opens the client session if it hasn't been opened yet,
           and returns the client session.
           Async session needs to be created on the event loop.
           We cannot create this session in the constructor since
           python constructors don't support async-await paradigm.
        Returns:
            The cached client session.
        """      
        if self._session is not None:
            return self._session

        # Default request headers
        json_headers = {
            "Content-Type": "application/json",
            "Accept": "application/json",
        }    
        
        self._session = ClientSession(headers=json_headers, auth=self.auth)
        return self._session

    async def close_session(self):
        if self._session is not None:
            await self._session.close()  

    async def get_user_profile(self, id: Text) -> Dict[Text, Any]:
        """Get the user profile associated with the given ID.
        Args:
            id: Service now sys_id used to retrieve the user profile.            
        Returns:
            A dictionary with user profile information.
        """
        
        url = f"{self.base_api_url}/table/sys_user/{id}"
        session = await self.open_session()

        async with session.get(url) as resp:
            if resp.status != 200:
                logger.error("Unable to load user profile. Status: %d", resp.status)
                return anonymous_profile
            
            resp_json = await resp.json()
            user = resp_json.get("result")            
            user_profile = {
                "id": id,
                "name": user.get("name"),
                "email": user.get("email")
            }
            return user_profile

    async def retrieve_incidents(self, user_profile) -> Dict[Text, Any]:
        caller_id = user_profile.get("id")
        url = (
            f"{self.base_api_url}/table/incident?"
            f"sysparm_query=caller_id={caller_id}"
            f"&sysparm_display_value=true"
        )
        session = await self.open_session()        
        async with session.get(url) as resp:
            if resp.status != 200:
                return { "error": "Unable to get recent incidents"}
            
            resp_json = await resp.json()    
            result = resp_json.get("result")
            if result:
                return { "incidents": result }
            else:
                email = user_profile.get("email")
                return { "error": f"No incidents on record for {email}" }

    async def create_incident(
        self,
        caller_id,
        short_description,
        description,
        priority
    ) -> Dict[Text, Any]:
        url = f"{self.base_api_url}/table/incident"
        data={
            "short_description": short_description,
            "description": description,
            "urgency": priorities.get(priority),
            "opened_by": caller_id,
            "caller_id": caller_id,
            "comments": "Rasa assistant opened this ticket"
        }
        session = await self.open_session()        
        async with session.post(url, json=data) as resp:
            if resp.status != 201:
                resp_json = await resp.json()
                logger.error(
                    "Unable to create incident. Status: %d; Error: %s",
                    resp.status,
                    resp_json
                )
                return { "error": "Unable to create incident"}
            
            resp_json = await resp.json()            
            return resp_json.get("result", {})

    @staticmethod
    def priority_db() -> Dict[str, int]:
        """Database of supported priorities"""        
        return priorities

    @staticmethod
    def states_db() -> Dict[str, str]:
        """Database of supported states"""
        return states
