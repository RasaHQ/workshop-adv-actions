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
import com.rasa.workshop.common.Constants;
import com.rasa.workshop.common.Document;
import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import com.rasa.workshop.common.Utils;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonDB
    implements DB {

  private static final Logger LOGGER = Logger.getLogger(JsonDB.class.getName());

  private final String mName;
  private final File mRootFolder;
  private final ReentrantReadWriteLock mReadWriteLock;

  public JsonDB(JsonObject pConfig) {
    mName = pConfig.getString(Constants.DB_NAME_KEY, Constants.DEFAULT_DB_NAME_VALUE);
    String stateFolder = pConfig.getString(Constants.STATE_FOLDER_KEY, Constants.DEFAULT_STATE_FOLDER_VALUE);
    mRootFolder = new File(stateFolder, mName);
    mReadWriteLock = new ReentrantReadWriteLock();

    if (!mRootFolder.exists() && !mRootFolder.mkdirs()) {
      throw new RuntimeException("Unable to initialize DB. Failed to create root folder.");
    }
  }

  @Override
  public String name() {
    return mName;
  }

  @Override
  public Document createDocument(Document pDoc)
      throws DocumentExistsException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(new File(mRootFolder, pDoc.collectionId()), pDoc.id());
      if (file.exists()) {
        throw new DocumentExistsException("There's an existing document: " + pDoc.id());
      }

      Utils.jsonToFile(file, pDoc.payload());

      return Document
          .newBuilder()
          .underCollection(pDoc.collectionId())
          .withId(pDoc.id())
          .withPayload(pDoc.payload())
          .build();

    } catch (IOException ex) {
      throw new DocumentException("Unable to create document: " + pDoc.id(), ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Document getDocument(String pCollectionId, String pDocId)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.readLock();
    lock.lock();

    try {
      File file = new File(new File(mRootFolder, pCollectionId), pDocId);

      if (!file.exists()) {
        throw new DocumentNotFoundException("Document not found: " + pDocId);
      }

      return Document
          .newBuilder()
          .underCollection(pCollectionId)
          .withId(pDocId)
          .withPayload(Utils.fileToJson(file))
          .build();

    } catch (IOException ex) {
      throw new DocumentException("Unable to get document: " + pDocId, ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Document updateDocument(Document pDoc)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(new File(mRootFolder, pDoc.collectionId()), pDoc.id());

      if (!file.exists()) {
        throw new DocumentNotFoundException("Document not found: " + pDoc.id());
      }

      Utils.jsonToFile(file, pDoc.payload());

      return Document
          .newBuilder()
          .underCollection(pDoc.collectionId())
          .withId(pDoc.id())
          .withPayload(pDoc.payload())
          .build();
    } catch (IOException ex) {
      throw new DocumentException("Unable to update document: " + pDoc.id(), ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Document deleteDocument(String pCollectionId, String pDocId)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(new File(mRootFolder, pCollectionId), pDocId);

      if (!file.exists()) {
        throw new DocumentNotFoundException("Document not found: " + pDocId);
      }

      var json = Utils.fileToJson(file);

      if (!file.delete()) {
        throw new DocumentException("Unable to delete document: " + pDocId);
      }

      return Document
          .newBuilder()
          .underCollection(pCollectionId)
          .withId(pDocId)
          .withPayload(json)
          .build();

    } catch (IOException ex) {
      throw new DocumentException("Unable to delete document: " + pDocId, ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean hasDocument(String pCollectionId, String pDocId) {
    Lock lock = mReadWriteLock.readLock();
    lock.lock();

    try {
      return new File(new File(mRootFolder, pCollectionId), pDocId).exists();

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Collection createCollection(Collection pCol)
      throws DocumentExistsException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(mRootFolder, pCol.id());
      if (file.exists()) {
        throw new DocumentExistsException("There's an existing collection: " + pCol.id());
      }

      if (!file.mkdir()) {
        throw new DocumentException("Unable to create collection: " + pCol.id());
      }

      return Collection
          .newBuilder()
          .withId(pCol.id())
          .withPayload(new JsonObject())
          .build();

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Collection getCollection(String pId)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.readLock();
    lock.lock();

    try {
      File file = new File(mRootFolder, pId);
      if (!file.exists()) {
        throw new DocumentNotFoundException("Collection not found: " + pId);
      }

      var builder = Collection
          .newBuilder()
          .withId(pId)
          .withPayload(new JsonObject());

      Files.walk(file.toPath()).skip(1).forEach(path -> {
        File docFile = path.toFile();
        try {
          var json = Utils.fileToJson(docFile);
          var doc = Document
              .newBuilder()
              .underCollection(pId)
              .withId(docFile.getName())
              .withPayload(json)
              .build();
          builder.addDocument(doc);

        } catch (IOException ex) {
          LOGGER.log(Level.SEVERE, "Unable to read JSON from " + docFile, ex);
        }
      });

      return builder.build();

    } catch (IOException ex) {
      throw new DocumentException("Unable to get collection: " + pId, ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Collection updateCollection(Collection pCol)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(mRootFolder, pCol.id());
      if (!file.exists()) {
        throw new DocumentNotFoundException("Collection not found: " + pCol.id());
      }

      return Collection
          .newBuilder()
          .withId(pCol.id())
          .build();

    } finally {
      lock.unlock();
    }
  }

  @Override
  public Collection deleteCollection(String pId)
      throws DocumentNotFoundException, DocumentException {
    Lock lock = mReadWriteLock.writeLock();
    lock.lock();

    try {
      File file = new File(mRootFolder, pId);
      if (!file.exists()) {
        throw new DocumentNotFoundException("Collection not found: " + pId);
      }

      Utils.deleteFolder(file);

      return Collection
          .newBuilder()
          .withId(pId)
          .build();

    } catch (IOException ex) {
      throw new DocumentException("Unable to delete collection: " + pId, ex);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean hasCollection(String pId) {
    Lock lock = mReadWriteLock.readLock();
    lock.lock();

    try {
      return new File(mRootFolder, pId).exists();

    } finally {
      lock.unlock();
    }
  }
}
