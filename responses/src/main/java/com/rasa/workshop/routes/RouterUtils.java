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

import com.rasa.workshop.common.Utils;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;

public class RouterUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterUtils.class);

  public static void configureBody(Router pRouter, JsonObject pConfig) {
    BodyHandler bodyHandler = BodyHandler.create()
      .setBodyLimit(pConfig.getLong("bodyLimit", -1L));

    pRouter.route().handler(bodyHandler);
  }

  public static void configureCORS(Router pRouter, JsonObject pConfig) {
    JsonObject corsConfig = pConfig.getJsonObject("cors", Utils.EMPTY_JSON);

    if (corsConfig.getBoolean("enabled", true)) {
      Set<HttpMethod> allowedMethods = new HashSet<>();
      allowedMethods.add(HttpMethod.GET);
      allowedMethods.add(HttpMethod.POST);
      allowedMethods.add(HttpMethod.PUT);
      allowedMethods.add(HttpMethod.DELETE);

      Set<String> allowedHeaders = new HashSet<>();
      allowedHeaders.add("Content-Type");
      allowedHeaders.add("Authorization");

      String corsOriginPattern = corsConfig.getString("originPattern", "*");

      CorsHandler corsHandler = CorsHandler.create(corsOriginPattern)
        .allowedHeaders(allowedHeaders)
        .allowedMethods(allowedMethods);

      if ("*".equals(corsOriginPattern)) {
        LOGGER.warn("CORS handler configured to allow requests from ANY origin.");

      } else {
        corsHandler.allowCredentials(true);
      }

      pRouter.route().handler(corsHandler);
    }
  }
}
