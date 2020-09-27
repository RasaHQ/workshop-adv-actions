package com.rasa.workshop.rasa;

import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ActionsRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActionsRunner.class);

  private final Map<String, Action> mActionsMap;

  public ActionsRunner(Action... actions) {
    Map<String, Action> actionsMap = new HashMap<>();

    for (Action action : actions) {
      actionsMap.put(action.name(), action);
      LOGGER.info("{0} action registered", action.name());
    }

    this.mActionsMap = Collections.unmodifiableMap(actionsMap);
  }

  public JsonObject run(JsonObject pJsonRequest)
    throws ActionExecutionRejectionException,
    UnsupportedOperationException,
          DocumentException,
          DocumentNotFoundException,
          DocumentExistsException {

    ActionRequest actionRequest = ActionRequest.newRequest(pJsonRequest);
    Action action = mActionsMap.get(actionRequest.nextAction());

    if (action == null) {
      throw new UnsupportedOperationException(String.format("Action %s not found", actionRequest.nextAction()));
    }

    ActionResult actionResult = action.run(actionRequest);
    return actionResult.toJson();
  }
}

