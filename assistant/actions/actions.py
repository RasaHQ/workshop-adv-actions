import logging
from typing import Dict, Text, Any, List, Union, Optional
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.forms import FormAction, REQUESTED_SLOT
from rasa_sdk.events import AllSlotsReset, SlotSet, EventType, SessionStarted, ActionExecuted
from actions.snow import SnowAPI
from actions.util import anonymous_profile

logger = logging.getLogger(__name__)
snow = SnowAPI()

def get_user_id_from_event(tracker: Tracker) -> Text:
    """Pulls "session_started" event, if available, and 
       returns the userId from the channel's metadata.
       Anonymous user profile ID is returned if channel 
       metadata is not available
    """
    event = tracker.get_last_event_for("session_started")
    if event is not None:
        # Read the channel's metadata.
        metadata = event.get("metadata", {})
        # If "usedId" key is missing, return anonymous ID.
        return metadata.get("userId", anonymous_profile.get("id"))

    return anonymous_profile.get("id")

class ActionSessionStart(Action):
    def name(self) -> Text:
        return "action_session_start"

    @staticmethod
    async def fetch_slots(tracker: Tracker) -> List[EventType]:
        """Add user profile to the slots if it is not set."""

        slots = []

        # Start by copying all the existing slots
        for key in tracker.current_slot_values().keys():
            slots.append(SlotSet(key=key, value=tracker.get_slot(key)))

        return slots
         
    async def run(
        self,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: Dict[Text, Any],
    ) -> List[EventType]:

        # the session should begin with a `session_started` event
        events = [SessionStarted()]

        # any slots that should be carried over should come after the
        # `session_started` event
        newEvents = await self.fetch_slots(tracker)
        events.extend(newEvents)

        # an `action_listen` should be added at the end as a user message follows
        events.append(ActionExecuted("action_listen"))

        return events

class IncidentStatus(Action):
    def name(self) -> Text:
        return "action_incident_status"

    async def run(
        self,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: Dict[Text, Any],
    ) -> List[EventType]:
        """Look up all incidents associated with email address
           and return status of each"""

        return []

class OpenIncidentForm(FormAction):
    def name(self) -> Text:
        return "open_incident_form"

    @staticmethod
    def required_slots(tracker: Tracker) -> List[Text]:
        """A list of required slots that the form has to fill"""

        return []

    def slot_mappings(self) -> Dict[Text, Union[Dict, List[Dict]]]:
        """A dictionary to map required slots to
            - an extracted entity
            - intent: value pairs
            - a whole message
            or a list of them, where a first match will be picked"""

        return {
            "incident_title": [
                self.from_trigger_intent(
                    intent="password_reset",
                    value="Problem resetting password",
                ),
                self.from_trigger_intent(
                    intent="problem_email", value="Problem with email"
                ),
                self.from_text(
                    not_intent=[
                        "incident_status",
                        "bot_challenge",
                        "help",
                        "affirm",
                        "deny",
                    ]
                ),
            ],
            "problem_description": [
                self.from_text(
                    not_intent=[
                        "incident_status",
                        "bot_challenge",
                        "help",
                        "affirm",
                        "deny",
                    ]
                )
            ],
            "priority": self.from_entity(entity="priority"),
            "confirm": [
                self.from_intent(value=True, intent="affirm"),
                self.from_intent(value=False, intent="deny"),
            ],
        }

    def validate_priority(
        self,
        value: Text,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: Dict[Text, Any],
    ) -> Dict[Text, Any]:
        """Validate priority is a valid value."""

        return {}

    def build_slot_sets(self, user_profile) -> List[Dict]:  
        """Helper method to build slot sets"""
        return [
            AllSlotsReset(),
            SlotSet("user_profile", user_profile),
            SlotSet("user_name", user_profile.get("name"))
        ]   

    async def submit(
        self,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: Dict[Text, Any],
    ) -> List[Dict]:
        """Create an incident and return the details"""

        return []
