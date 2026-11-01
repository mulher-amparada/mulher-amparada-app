/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.healthconnect.exportimport;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.health.connect.exportimport.ExportImportDocumentProvider;
import android.net.Uri;
import android.os.DeadObjectException;
import android.provider.DocumentsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages querying the document providers available to use for export/import.
 *
 * @hide
 */
public final class DocumentProvidersManager {
    private static final String REQUIRED_MIME_TYPE = "application/zip";

    /** Returns the document providers available to be used for export/import. */
    public static List<ExportImportDocumentProvider> queryDocumentProviders(Context context) {
        ArrayList<ExportImportDocumentProvider> documentProviders = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        ContentResolver contentResolver = context.getContentResolver();

        Intent intent = new Intent(DocumentsContract.PROVIDER_INTERFACE);
        List<ResolveInfo> providers =
                packageManager.queryIntentContentProviders(intent, /* flags= */ 0);
        for (ResolveInfo info : providers) {
            ProviderInfo providerInfo = info.providerInfo;
            String authority = providerInfo.authority;
            if (authority != null) {
                readDocumentProviders(
                        documentProviders, contentResolver, authority, /* attempt= */ 0);
            }
        }

        return documentProviders;
    }

    private static void readDocumentProviders(
            ArrayList<ExportImportDocumentProvider> documentProviders,
            ContentResolver contentResolver,
            String authority,
            int attempt) {
        try (ContentProviderClient contentProviderClient =
                contentResolver.acquireUnstableContentProviderClient(authority)) {
            if (contentProviderClient != null) {
                Uri rootsUri = DocumentsContract.buildRootsUri(authority);
                try (Cursor cursor =
                        contentProviderClient.query(rootsUri, null, null, null, null)) {
                    if (cursor != null) {
                        readDocumentProvidersFromCursor(documentProviders, cursor, authority);
                    }
                } catch (DeadObjectException e) {
                    if (attempt == 0) {
                        // The system can return a content provider that's gone away. Acquiring the
                        // content provider after the DeadObjectException will try to restart the
                        // content provider.
                        readDocumentProviders(
                                documentProviders, contentResolver, authority, /* attempt= */ 1);
                    }
                    // Ignore exception on second attempt so successful document provider queries
                    // are returned.
                } catch (Exception ignored) {
                    // Ignore exception so successful document provider queries are returned.
                }
            }
        }
    }

    private static void readDocumentProvidersFromCursor(
            ArrayList<ExportImportDocumentProvider> documentProviders,
            Cursor cursor,
            String authority) {
        while (cursor.moveToNext()) {
            if (!isDocumentProviderSupported(cursor)) {
                continue;
            }

            int titleIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_TITLE);
            int summaryIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_SUMMARY);
            int iconResourceIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_ICON);
            int rootDocumentIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_ROOT_ID);

            if (titleIndex == -1 || iconResourceIndex == -1 || rootDocumentIndex == -1) {
                // These columns are required but this isn't enforced. Skip document providers that
                // don't follow these requirements.
                continue;
            }

            String title = cursor.getString(titleIndex);
            String summary = summaryIndex != -1 ? cursor.getString(summaryIndex) : "";
            // Returned summary can be null even though the column is present.
            if (summary == null) {
                summary = "";
            }
            int iconResource = cursor.getInt(iconResourceIndex);
            String rootDocument = cursor.getString(rootDocumentIndex);
            Uri rootDocumentUri = DocumentsContract.buildRootUri(authority, rootDocument);

            documentProviders.add(
                    new ExportImportDocumentProvider(
                            title, summary, iconResource, rootDocumentUri, authority));
        }
    }

    private static boolean isDocumentProviderSupported(Cursor cursor) {
        int flagsIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_FLAGS);
        int flags = flagsIndex != -1 ? cursor.getInt(flagsIndex) : 0;

        if ((flags & DocumentsContract.Root.FLAG_LOCAL_ONLY)
                == DocumentsContract.Root.FLAG_LOCAL_ONLY) {
            return false;
        }

        if ((flags & DocumentsContract.Root.FLAG_SUPPORTS_CREATE)
                != DocumentsContract.Root.FLAG_SUPPORTS_CREATE) {
            return false;
        }

        int mimeTypesIndex = cursor.getColumnIndex(DocumentsContract.Root.COLUMN_MIME_TYPES);
        String mimeTypes = mimeTypesIndex != -1 ? cursor.getString(mimeTypesIndex) : null;
        if (mimeTypes != null) {
            return mimeTypes.lines().anyMatch(s -> s.equalsIgnoreCase(REQUIRED_MIME_TYPE));
        }

        return true;
    }
}
