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

import com.rasa.workshop.common.Collection;
import com.rasa.workshop.common.Document;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rasa.workshop.common.Utils.documentToJson;

/**
 * Base class that provides common methods to write back REST API responses to the client.
 */
public abstract class ApiRouter {

  static final String ID_PARAM = "id";
  static final String ID_PATH = "/:" + ID_PARAM;

  static final String CONTENT_TYPE = "Content-Type";
  static final String CONTENT_JSON = "application/json";

  protected final Vertx mVertx;
  protected final Router mRouter;
  protected final Logger mLogger;
  private final JWTAuth mJwtAuth;

  ApiRouter(Vertx pVertx, Logger pLogger, JWTAuth pJwtAuth, List<String> pAuthorities)
    throws Exception {
    this.mVertx = pVertx;
    this.mRouter = Router.router(pVertx);
    this.mLogger = pLogger;
    this.mJwtAuth = pJwtAuth;

    if (pJwtAuth != null) {
      secureBaseRoute(pAuthorities != null ? new HashSet<>(pAuthorities) : null);

    } else {
      pLogger.warn("Router is not backed by an auth handler: " + basePath());
    }

    configureRoutes(basePath(), pVertx);
  }

  protected abstract String basePath();

  protected abstract void configureRoutes(String pCollectionPath, Vertx pVertx);

  protected void secureBaseRoute(Set<String> pAuthorities) {
    AuthHandler jwtAuthHandler = JWTAuthHandler.create(mJwtAuth);

    if (pAuthorities != null) {
      jwtAuthHandler.addAuthorities(pAuthorities);
    }

    mRouter.route(basePath() + "/*").handler(jwtAuthHandler);
  }

  public Router getRouter() {
    return mRouter;
  }

  void sendCollection(Collection pCollection, HttpServerResponse pResp, int pStatus) {
    var result = new JsonObject();
    result.put("item", documentToJson(pCollection));

    var itemsJson = new JsonArray();
    pCollection.documents().forEach(doc -> itemsJson.add(documentToJson(doc)));
    result.put("items", itemsJson);

    pResp.setChunked(true);
    pResp.setStatusCode(pStatus);
    pResp.putHeader(CONTENT_TYPE, CONTENT_JSON);
    pResp.write(result.toBuffer()).end();
  }

  void sendDocument(Document pDocument, HttpServerResponse pResp, int pStatus) {
    var result = new JsonObject();
    result.put("item", documentToJson(pDocument));
    pResp.setChunked(true);
    pResp.setStatusCode(pStatus);
    pResp.putHeader(CONTENT_TYPE, CONTENT_JSON);
    pResp.write(result.toBuffer()).end();
  }

  void sendError(Throwable pEx, HttpServerResponse pResp) {
    var result = new JsonObject();

    if (pEx instanceof DocumentNotFoundException) {
      result.put("error", pEx.getMessage());
      pResp.setStatusCode(404);
    } else if (pEx instanceof DocumentExistsException) {
      result.put("error", pEx.getMessage());
      pResp.setStatusCode(409);
    } else {
      mLogger.error(pEx.getMessage(), pEx);
      result.put("error", "Unable to process request");
      pResp.setStatusCode(500);
    }

    pResp.putHeader(CONTENT_TYPE, CONTENT_JSON);
    pResp.setChunked(true);
    pResp.write(result.toBuffer()).end();
  }
}
