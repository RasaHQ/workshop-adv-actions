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

package com.rasa.workshop;

import com.rasa.workshop.db.DB;
import com.rasa.workshop.routes.ResponsesRouter;
import com.rasa.workshop.routes.RouterUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;

public class RasaResponsesServer
  extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(RasaResponsesServer.class);

  @Override
  public void start(Future<Void> pStartFuture) {
    try {
      JWTAuth jwtAuth = null;
      Router mainRouter = Router.router(vertx);
      DB db = DB.newDB(config());
      RouterUtils.configureBody(mainRouter, config());
      RouterUtils.configureCORS(mainRouter, config());
      mainRouter.mountSubRouter("/api/v1", new ResponsesRouter(vertx, jwtAuth, db).getRouter());
      serve(pStartFuture, mainRouter);

    } catch (Exception ex) {
      LOGGER.error("Unable to start rasa responses server", ex);
      pStartFuture.fail(ex);
    }
  }

  private void serve(Future<Void> pStartFuture, Router pRouter) {
    HttpServer httpServer = vertx.createHttpServer()
      .requestHandler(pRouter::accept);

    httpServer.listen(8080, lh -> {
      if (lh.succeeded()) {
        LOGGER.info(String.format("Rasa responses server now serving requests on %d", lh.result().actualPort()));

        pStartFuture.complete();

      } else {
        pStartFuture.fail(lh.cause());
      }
    });
  }
}
