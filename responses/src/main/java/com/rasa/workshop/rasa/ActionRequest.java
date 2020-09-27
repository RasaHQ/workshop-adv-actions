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

package com.rasa.workshop.rasa;

import io.vertx.core.json.JsonObject;

public class ActionRequest {

  private final String mNextAction;
  private final String mSenderId;
  private final Tracker mTracker;
  private final Domain mDomain;

  private ActionRequest(String pNextAction,
                        String pSenderId,
                        Tracker pTracker,
                        Domain pDomain) {
    this.mNextAction = pNextAction;
    this.mSenderId = pSenderId;
    this.mTracker = pTracker;
    this.mDomain = pDomain;
  }

  public String nextAction() {
    return mNextAction;
  }

  public Tracker tracker() {
    return mTracker;
  }

  public Domain domain() {
    return mDomain;
  }

  public static ActionRequest newRequest(JsonObject pJson) {
    Tracker tracker = new Tracker(pJson.getJsonObject("tracker"));
    Domain domain = new Domain(pJson.getJsonObject("domain"));

    return new ActionRequest(pJson.getString("next_action", ""),
      pJson.getString("sender_id", null),
      tracker,
      domain);
  }
}
