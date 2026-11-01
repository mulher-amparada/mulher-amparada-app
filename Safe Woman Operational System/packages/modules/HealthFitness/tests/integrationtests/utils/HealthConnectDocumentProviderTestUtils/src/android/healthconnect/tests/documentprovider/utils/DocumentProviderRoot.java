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

import androidx.annotation.Nullable;

/** Root for a document provider. */
public final class DocumentProviderRoot {
    private static final String HAS_SUMMARY_KEY = "has_summary";

    @Nullable private String mRootId;
    @Nullable private String mTitle;
    @Nullable private String mSummary;
    private boolean mHasSummary;
    @Nullable private Integer mFlags;
    @Nullable private Integer mIconResourceId;
    @Nullable private String mMimeTypes;

    public DocumentProviderRoot() {}

    public DocumentProviderRoot(Bundle bundle) {
        mRootId = bundle.getString(DocumentsContract.Root.COLUMN_ROOT_ID);
        mTitle = bundle.getString(DocumentsContract.Root.COLUMN_TITLE);
        mSummary = bundle.getString(DocumentsContract.Root.COLUMN_SUMMARY);
        mHasSummary = bundle.getBoolean(HAS_SUMMARY_KEY, true);

        if (bundle.containsKey(DocumentsContract.Root.COLUMN_FLAGS)) {
            mFlags = bundle.getInt(DocumentsContract.Root.COLUMN_FLAGS);
        }

        if (bundle.containsKey(DocumentsContract.Root.COLUMN_ICON)) {
            mIconResourceId = bundle.getInt(DocumentsContract.Root.COLUMN_ICON);
        }

        mMimeTypes = bundle.getString(DocumentsContract.Root.COLUMN_MIME_TYPES);
    }

    /** Builds the document provider root as a row in the cursor. */
    public void build(MatrixCursor.RowBuilder row) {
        if (mRootId != null) {
            row.add(DocumentsContract.Root.COLUMN_ROOT_ID, mRootId);
        }

        if (mTitle != null) {
            row.add(DocumentsContract.Root.COLUMN_TITLE, mTitle);
        }

        if (mHasSummary) {
            row.add(DocumentsContract.Root.COLUMN_SUMMARY, mSummary);
        }

        if (mFlags != null) {
            row.add(DocumentsContract.Root.COLUMN_FLAGS, mFlags);
        }

        if (mIconResourceId != null) {
            row.add(DocumentsContract.Root.COLUMN_ICON, mIconResourceId);
        }

        if (mMimeTypes != null) {
            row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, mMimeTypes);
        }
    }

    /** Sets the id of the root. */
    public DocumentProviderRoot setRootId(String rootId) {
        mRootId = rootId;
        return this;
    }

    /** Sets the title of the root. */
    public DocumentProviderRoot setTitle(String title) {
        mTitle = title;
        return this;
    }

    /** Sets the summary of the root. */
    public DocumentProviderRoot setSummary(@Nullable String summary) {
        mSummary = summary;
        mHasSummary = true;
        return this;
    }

    /** Sets the flags for the root. */
    public DocumentProviderRoot setFlags(int flags) {
        mFlags = flags;
        return this;
    }

    /** Sets the icon resource for the root. */
    public DocumentProviderRoot setIconResourceId(int iconResourceId) {
        mIconResourceId = iconResourceId;
        return this;
    }

    /** Sets the MIME types supported by the root. */
    public DocumentProviderRoot setMimeTypes(String mimeTypes) {
        mMimeTypes = mimeTypes;
        return this;
    }

    /** Returns a bundle representation of the root. */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mRootId != null) {
            bundle.putString(DocumentsContract.Root.COLUMN_ROOT_ID, mRootId);
        }

        if (mTitle != null) {
            bundle.putString(DocumentsContract.Root.COLUMN_TITLE, mTitle);
        }

        if (mSummary != null) {
            bundle.putString(DocumentsContract.Root.COLUMN_SUMMARY, mSummary);
        }
        bundle.putBoolean(HAS_SUMMARY_KEY, mHasSummary);

        if (mFlags != null) {
            bundle.putInt(DocumentsContract.Root.COLUMN_FLAGS, mFlags);
        }

        if (mIconResourceId != null) {
            bundle.putInt(DocumentsContract.Root.COLUMN_ICON, mIconResourceId);
        }

        if (mMimeTypes != null) {
            bundle.putString(DocumentsContract.Root.COLUMN_MIME_TYPES, mMimeTypes);
        }

        return bundle;
    }
}
