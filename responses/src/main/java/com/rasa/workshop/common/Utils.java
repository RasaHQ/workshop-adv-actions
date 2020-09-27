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

package com.rasa.workshop.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

  public static final JsonObject EMPTY_JSON = new JsonObject(Map.of());
  public static final JsonArray EMPTY_JSON_ARRAY = new JsonArray(List.of());

  public static String generateId() {
    return UUID.randomUUID().toString();
  }

  private static final Set<String> IGNORED_FOLDERS = Set.of(
    ".git",
    ".DS_Store",
    "models"
  );

  public static JsonObject documentToJson(Document pDocument) {
    var json = new JsonObject(pDocument.payload().getMap());
    json.put("id", pDocument.id());
    return json;
  }

  public static void jsonToFile(File pFile, JsonObject pJson)
    throws IOException {
    Files.writeString(pFile.toPath(), pJson.encodePrettily());
  }

  public static JsonObject fileToJson(File pFile)
      throws IOException {
    var rawJson = Files.readString(pFile.toPath());
    return new JsonObject(rawJson);
  }

  public static void deleteFolder(File pFolder)
      throws IOException {
    Files
        .walk(pFolder.toPath())
        .sorted(Comparator.reverseOrder())
        .forEach(path -> path.toFile().delete());
  }

  public static void zip(File pSrc, File pZipFile)
    throws IOException {
    try (FileOutputStream outputStream = new FileOutputStream(pZipFile);
         ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      zip(pSrc, zipOutputStream, pSrc.getCanonicalPath().length() + 1);
    }
  }

  private static void zip(File pSrc, ZipOutputStream pOutputStream, int pFilePathStartIndex)
    throws IOException {
    if (pSrc.isDirectory()) {
      if (IGNORED_FOLDERS.contains(pSrc.getName())) return;

      var files = pSrc.listFiles();
      if (files == null) return;

      for (File file : files) {
        zip(file, pOutputStream, pFilePathStartIndex);
      }
    } else if (pSrc.isFile()) {
      zipEntry(pSrc, pOutputStream, pFilePathStartIndex);
    }
  }

  private static void zipEntry(File pFile, ZipOutputStream pOutputStream, int pFilePathStartIndex)
    throws IOException {
    String zipEntryFilePath = pFile.getCanonicalPath().substring(pFilePathStartIndex);
    ZipEntry zipEntry = new ZipEntry(zipEntryFilePath);

    try (FileInputStream inputStream = new FileInputStream(pFile)) {
      pOutputStream.putNextEntry(zipEntry);
      var bytes = new byte[2048];
      int length;
      while ((length = inputStream.read(bytes)) >= 0) {
        pOutputStream.write(bytes, 0, length);
      }
      pOutputStream.closeEntry();
    }
  }
}
