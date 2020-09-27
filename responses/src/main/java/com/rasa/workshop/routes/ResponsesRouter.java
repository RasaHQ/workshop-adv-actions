/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rasa.workshop.routes;

import com.rasa.workshop.db.DB;
import com.rasa.workshop.service.ResponsesService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;

public class ResponsesRouter
  extends ApiRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResponsesRouter.class);

  private final ResponsesService mService;

  public ResponsesRouter(Vertx pVertx, JWTAuth pJwtAuth, DB pDB)
    throws Exception {
    super(pVertx, LOGGER, pJwtAuth, null);

    mService = new ResponsesService(pDB);
  }

  @Override
  protected String basePath() {
    return "/responses";
  }

  @Override
  protected void configureRoutes(String pCollectionPath, Vertx pVertx) {
    configureBotResponseRoute();
  }

  private void configureBotResponseRoute() {
    mRouter.route(HttpMethod.POST, basePath() + ID_PATH).handler(routingContext -> {
      LOGGER.info("POST " + routingContext.request().path());
      JsonObject payload = routingContext.getBodyAsJson();
      String botId = routingContext.pathParam(ID_PARAM);

      try {
        JsonObject botResponse = mService.generateResponse(botId, payload);

        routingContext.response().setStatusCode(200);
        routingContext.response().putHeader(CONTENT_TYPE, CONTENT_JSON);
        routingContext.response().setChunked(true);
        routingContext.response().write(botResponse.toBuffer()).end();

      } catch (Exception ex) {
        sendError(ex, routingContext.response());
      }
    });
  }
}
