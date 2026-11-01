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

package android.healthconnect.tests.documentproviderapp2;

import android.database.Cursor;
import android.healthconnect.tests.documentprovider.utils.FakeDocumentProviderRoots;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;

/** Document provider used for tests. Configured by {@link FakeDocumentProviderRoots}. */
public final class TestDocumentProvider extends DocumentsProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openDocument(
            String documentId, String mode, CancellationSignal signal) {
        throw new UnsupportedOperationException("Open document isn't supported");
    }

    @Override
    public Cursor queryChildDocuments(
            String parentDocumentId, String[] projection, String sortOrder) {
        throw new UnsupportedOperationException("Query child documents isn't supported");
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) {
        throw new UnsupportedOperationException("Query document isn't supported");
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        return FakeDocumentProviderRoots.INSTANCE.getCursor();
    }
}
