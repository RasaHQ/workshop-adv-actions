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

package com.rasa.workshop.db;

import com.rasa.workshop.common.Collection;
import com.rasa.workshop.common.Document;
import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import io.vertx.core.json.JsonObject;

public interface DB {

  String name();

  Document createDocument(Document pDoc)
      throws DocumentExistsException, DocumentException;

  Document getDocument(String pCollectionId, String pDocId)
      throws DocumentNotFoundException, DocumentException;

  Document updateDocument(Document pDoc)
      throws DocumentNotFoundException, DocumentException;

  Document deleteDocument(String pCollectionId, String pDocId)
      throws DocumentNotFoundException, DocumentException;

  boolean hasDocument(String pCollectionId, String pDocId);

  Collection createCollection(Collection pCol)
      throws DocumentExistsException, DocumentException;

  Collection getCollection(String pId)
      throws DocumentNotFoundException, DocumentException;

  Collection updateCollection(Collection pCol)
      throws DocumentNotFoundException, DocumentException;

  Collection deleteCollection(String pId)
      throws DocumentNotFoundException, DocumentException;

  boolean hasCollection(String pId);

  static DB newDB(JsonObject pConfig) {
    return new JsonDB(pConfig);
  }
}
