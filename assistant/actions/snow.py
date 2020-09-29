import asyncio
import logging
import signal
from typing import Dict, Text, Any
from aiohttp import ClientSession, BasicAuth
from actions.util import anonymous_profile, priorities, states

logger = logging.getLogger(__name__)

class SnowAPI:
    """class to connect to the ServiceNow API"""

    def __init__(self):
        # TODO: Implement method
        self.base_api_url = None        
        self._session = None
        self._loop = None

    async def open_session(self) -> ClientSession:  
        """Opens the client session if it hasn't been opened yet,
           and returns the client session.
           Async session needs to be created on the event loop.
           We cannot create this session in the constructor since
           python constructors don't support async-await paradigm.
        Returns:
            The cached client session.
        """  
        # TODO: Implement method    
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
        # TODO: Implement method
        return anonymous_profile

    async def retrieve_incidents(self, user_profile) -> Dict[Text, Any]:
        # TODO: Implement method
        return {}

    async def create_incident(
        self,
        caller_id,
        short_description,
        description,
        priority
    ) -> Dict[Text, Any]:
        # TODO: Implement method
        return {}

    @staticmethod
    def priority_db() -> Dict[str, int]:
        """Database of supported priorities"""        
        return priorities

    @staticmethod
    def states_db() -> Dict[str, str]:
        """Database of supported states"""
        return states
