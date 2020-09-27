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

        user_profile = tracker.get_slot("user_profile")
        user_name = tracker.get_slot("user_name")

        if user_profile is None:
            id = get_user_id_from_event(tracker)
            if id == anonymous_profile.get("id"):
                user_profile = anonymous_profile
            else:    
                # Make an actual call to Snow API.
                user_profile = await snow.get_user_profile(id)

            slots.append(SlotSet(key="user_profile", value=user_profile))

        if user_name is None:
            slots.append(SlotSet(key="user_name", value=user_profile.get("name")))

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

        user_profile = tracker.get_slot("user_profile")

        # Handle anonymous profile. No need to call Snow API.
        if user_profile.get("id") == anonymous_profile.get("id"):
            message = "Since you are anonymous, I can't realy tell your incident status :)"
        else:
            incident_states = snow.states_db()
            incidents_result = await snow.retrieve_incidents(user_profile)
            incidents = incidents_result.get("incidents")
            if incidents:
                message = "\n".join(
                    [
                        f'Incident {i.get("number")}: '
                        f'"{i.get("short_description")}", '
                        f'opened on {i.get("opened_at")} '
                        f'{incident_states.get(i.get("incident_state"))}'
                        for i in incidents
                    ]
                )
            else:
                message = f"{incidents_result.get('error')}"

        dispatcher.utter_message(message)
        return []

class OpenIncidentForm(FormAction):
    def name(self) -> Text:
        return "open_incident_form"

    @staticmethod
    def required_slots(tracker: Tracker) -> List[Text]:
        """A list of required slots that the form has to fill"""

        return [
            "incident_title",
            "problem_description",
            "priority",
            "confirm"
        ]

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

        if value.lower() in snow.priority_db():
            return {"priority": value}
        else:
            dispatcher.utter_message(template="utter_no_priority")
            return {"priority": None}

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

        user_profile = tracker.get_slot("user_profile")
        confirm = tracker.get_slot("confirm")

        if not confirm:
            dispatcher.utter_message(
                template="utter_incident_creation_canceled"
            )
            # Early exit.
            return self.build_slot_sets(user_profile)

        # Handle anonymous profile. No need to call Snow API.
        if user_profile.get("id") == anonymous_profile.get("id"):
            message = (
                "Nice try anonymous. But I can't actually create a "
                "ticket for you. Appreciate your enthusiasm though :)"
            )
        else:
            result = await snow.create_incident( 
                user_profile.get("id"),           
                tracker.get_slot("incident_title"),
                tracker.get_slot("problem_description"),
                tracker.get_slot("priority")
            )
            incident_number = result.get("number")
            if incident_number:
                message = (
                    f"Incident {incident_number} has been opened for you. "
                    f"A support specialist will reach out to you soon."
                )
            else:
                message = (
                    f"Something went wrong while opening an incident for you. "
                    f"{result.get('error')}"
                )

        dispatcher.utter_message(message)
        return self.build_slot_sets(user_profile)
