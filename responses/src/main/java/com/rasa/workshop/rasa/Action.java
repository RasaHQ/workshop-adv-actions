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

import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;

public interface Action {

  /**
   * The next action to be taken in response to a dialogue state.
   *
   * @return the next action to be taken in response to a dialogue state.
   */
  String name();

  /**
   * Run the action and send a response back. Might include creating user messages and setting slots.
   *
   * @param pRequest the {@link ActionRequest} used to run this action.
   * @return the {@link ActionResult} of running this action.
   */
  ActionResult run(ActionRequest pRequest)
    throws ActionExecutionRejectionException, DocumentException, DocumentNotFoundException, DocumentExistsException;
}
