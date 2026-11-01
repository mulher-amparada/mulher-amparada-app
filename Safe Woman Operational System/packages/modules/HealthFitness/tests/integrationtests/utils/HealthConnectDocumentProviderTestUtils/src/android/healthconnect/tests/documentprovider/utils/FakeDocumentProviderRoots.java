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

package android.healthconnect.tests.documentprovider.utils;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.DocumentsContract;

import java.util.ArrayList;

/** Fake document provider roots that can be configured for tests. */
public final class FakeDocumentProviderRoots {
    public static FakeDocumentProviderRoots INSTANCE = new FakeDocumentProviderRoots();

    private static final String[] PROJECTION =
            new String[] {
                DocumentsContract.Root.COLUMN_ROOT_ID,
                DocumentsContract.Root.COLUMN_TITLE,
                DocumentsContract.Root.COLUMN_SUMMARY,
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.COLUMN_ICON,
                DocumentsContract.Root.COLUMN_MIME_TYPES,
            };

    private final ArrayList<DocumentProviderRoot> mRoots = new ArrayList<>();
    private boolean mThrowsException = false;

    private FakeDocumentProviderRoots() {}

    /** Returns a cursor filled with the configured roots. */
    public MatrixCursor getCursor() {
        if (mThrowsException) {
            throw new UnsupportedOperationException(
                    "TestDocumentProviderApp is configured to throw an exception");
        }

        final MatrixCursor cursor = new MatrixCursor(PROJECTION);

        for (DocumentProviderRoot root : mRoots) {
            root.build(cursor.newRow());
        }

        return cursor;
    }

    /** Configures {@link #getCursor()} to throw an exception. */
    public void setThrowsException() {
        mThrowsException = true;
    }

    /** Removes all roots and resets to the default configuration. */
    public void clear() {
        mRoots.clear();
        mThrowsException = false;
    }

    /** Adds the root defined in the bundle to the list of roots. */
    public void addRoot(Bundle bundle) {
        DocumentProviderRoot root = new DocumentProviderRoot(bundle);
        mRoots.add(root);
    }
}
