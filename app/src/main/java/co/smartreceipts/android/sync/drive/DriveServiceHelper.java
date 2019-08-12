/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Steven Hanus for Smart Receipts - 08/08/19
 */
package co.smartreceipts.android.sync.drive;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.sync.drive.rx.RxDriveTask;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {

  private static final int BYTE_BUFFER_SIZE = 8192;

  private final Context context;
  private final Executor mExecutor = Executors.newSingleThreadExecutor();
  private final Drive mDriveService;

  DriveServiceHelper(Context context, Drive driveService) {
    this.context = context;
    mDriveService = driveService;
  }

  /**
   * Creates a file in the user's My Drive folder and returns it.
   */
  //TODO: Determine if we need the entire file returned, if not, we can call setFields()
  //      on the execution to tell the api what data of the created file to return
  public Task<File> createFile(String name, String mimeType, String description,
                               Map<String, String> properties, String folderDestination,
                               java.io.File javaFile) {
    return Tasks.call(mExecutor, () -> {
      File metadata = new File();
      if (!TextUtils.isEmpty(folderDestination))
        metadata.setParents(Collections.singletonList(folderDestination));
      if (!TextUtils.isEmpty(mimeType))
        metadata.setMimeType(mimeType);
      if (!TextUtils.isEmpty(name))
        metadata.setName(name);
      if (!TextUtils.isEmpty(description))
        metadata.setDescription(description);
      if (properties != null)
        metadata.setAppProperties(properties);

      File googleFile;
      if (javaFile != null) {
        if (!TextUtils.isEmpty(mimeType)) {
          FileContent mediaContent = new FileContent(mimeType, javaFile);
          googleFile = mDriveService.files().create(metadata, mediaContent).execute();
        } else {
          final Uri uri = Uri.fromFile(javaFile);
          final String mime = UriUtils.getMimeType(uri, context.getContentResolver());
          metadata.setMimeType(mime);
          FileContent mediaContent = new FileContent(mime, javaFile);
          googleFile = mDriveService.files().create(metadata, mediaContent).execute();
        }
      } else {
        googleFile = mDriveService.files().create(metadata).execute();
      }

      if (googleFile == null) {
        throw new IOException("Null result when requesting file creation.");
      }

      return googleFile;
    });
  }

  /**
   * Returns a {@link FileList} containing all the visible files in the app folder.
   *
   * <p>The returned list will only contain files visible to this app, i.e. those which were
   * created by this app. To perform operations on files not created by the app, the project must
   * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
   * Developer's Console</a> and be submitted to Google for verification.</p>
   *
   */
  public Task<FileList> query(String query) {
    return Tasks.call(mExecutor, () ->
            mDriveService.files().list().setQ(query).setSpaces("appDataFolder").setFields("*").execute());
  }

  //TODO: Do we want to wrap a success listener around delete?
  public Task<Void> deleteFile(String fileId) {
    return Tasks.call(mExecutor, () ->
            mDriveService.files().delete(fileId).execute());
  }

  public Task<FileList> getAllFilesSortedByTime() {
    return Tasks.call(mExecutor, () ->
            mDriveService.files().list().setSpaces("appDataFolder").setOrderBy("modifiedTime").setFields("*").execute());
  }

  public Single<java.io.File> getDriveFileAsJavaFile(String fileId, java.io.File downloadLocationFile) {
    Task<InputStream> dlInputStreamTask = Tasks.call(mExecutor, () ->
            mDriveService.files().get(fileId).setFields("*").executeMediaAsInputStream());
    return RxDriveTask.toSingle(dlInputStreamTask)
            .flatMap(inputStream -> {
              FileOutputStream fileOutputStream = null;
              try {
                fileOutputStream = new FileOutputStream(downloadLocationFile);
                byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                  fileOutputStream.write(buffer, 0, read);
                }
                return Single.just(downloadLocationFile);

              } catch (IOException e) {
                Logger.error(DriveServiceHelper.this, "Failed write file with exception: ", e);
                return Single.error(e);
              } finally {
                StorageManager.closeQuietly(inputStream);
                StorageManager.closeQuietly(fileOutputStream);
              }
            });
  }

  public Task<File> getFile(String fileId) {
    return Tasks.call(mExecutor, () ->
            mDriveService.files().get(fileId).setFields("*").execute());
  }

  public Task<FileList> getFilesInFolder(String folderId) {
    String query = "'".concat(folderId).concat("' in parents");
    return Tasks.call(mExecutor, () ->
            mDriveService.files().list().setQ(query).setSpaces("appDataFolder").setFields("*").execute());
  }

  public Task<FileList> getFilesByNameInFolder(String folderId, String fileName) {
    String query = "'".concat(folderId).concat("' in parents and name = '".concat(fileName).concat("'"));
    return Tasks.call(mExecutor, () ->
            mDriveService.files().list().setQ(query).setSpaces("appDataFolder").setFields("*").execute());
  }

  public Single<File> updateFile(String fileId, java.io.File file) {

    final Uri uri = Uri.fromFile(file);
    final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());

    return RxDriveTask.toSingle(query("name = '".concat(file.getName()).concat("'")))
            .flatMap(fileList -> {
              if (fileList.getFiles().isEmpty()) {
                return RxDriveTask.toSingle(createFile(file.getName(), mimeType, null,
                        null, "appDataFolder", file));
              } else {
                  File driveFile = new File();
                  // File's new content.
                  FileContent mediaContent = new FileContent(mimeType, file);
                  driveFile.setMimeType(mimeType);
                  driveFile.setName(file.getName());
                  return RxDriveTask.toSingle(Tasks.call(mExecutor, () ->
                          mDriveService.files().update(fileId, driveFile, mediaContent).execute()));
              }
            });
  }

  //TODO: Verify if any of the following unused methods are better alternatives to current methods
//  /**
//   * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
//   * contents.
//   */
//  public Task<Pair<String, String>> readFile(String fileId) {
//    return Tasks.call(mExecutor, () -> {
//      // Retrieve the metadata as a File object.
//      File metadata = mDriveService.files().get(fileId).execute();
//      String name = metadata.getName();
//
//      // Stream the file contents to a String.
//      try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
//           BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
//        StringBuilder stringBuilder = new StringBuilder();
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//          stringBuilder.append(line);
//        }
//        String contents = stringBuilder.toString();
//
//        return Pair.create(name, contents);
//      }
//    });
//  }
//
//  /**
//   * Updates the file identified by {@code fileId} with the given {@code name} and {@code
//   * content}.
//   */
//  public Task<Void> saveFile(String fileId, String name, String content) {
//    return Tasks.call(mExecutor, () -> {
//      // Create a File containing any metadata changes.
//      File metadata = new File().setName(name);
//
//      // Convert content to an AbstractInputStreamContent instance.
//      ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
//
//      // Update the metadata and contents.
//      mDriveService.files().update(fileId, metadata, contentStream).execute();
//      return null;
//    });
//  }
//
//  /**
//   * Returns an {@link Intent} for opening the Storage Access Framework file picker.
//   */
//  public Intent createFilePickerIntent() {
//    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//    intent.addCategory(Intent.CATEGORY_OPENABLE);
//    intent.setType("text/plain");
//
//    return intent;
//  }
//
//  /**
//   * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
//   * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
//   */
//  public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
//          ContentResolver contentResolver, Uri uri) {
//    return Tasks.call(mExecutor, () -> {
//      // Retrieve the document's display name from its metadata.
//      String name;
//      try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
//        if (cursor != null && cursor.moveToFirst()) {
//          int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//          name = cursor.getString(nameIndex);
//        } else {
//          throw new IOException("Empty cursor returned for file.");
//        }
//      }
//
//      // Read the document's contents as a String.
//      String content;
//      try (InputStream is = contentResolver.openInputStream(uri);
//           BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
//        StringBuilder stringBuilder = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//          stringBuilder.append(line);
//        }
//        content = stringBuilder.toString();
//      }
//
//      return Pair.create(name, content);
//    });
//  }

}