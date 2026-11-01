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
package com.android.healthconnect.controller.exportimport.api

import android.net.Uri
import android.provider.DocumentsContract

private const val DOWNLOADS_AUTHORITY = "com.android.providers.downloads.documents"

/** Document providers for exporting/importing Health Connect data. */
sealed class DocumentProviders {
    data object Loading : DocumentProviders()

    data object LoadingFailed : DocumentProviders()

    data class WithData(val providers: List<DocumentProvider>) : DocumentProviders()
}

/** Single document provider for exporting/importing Health Connect data. */
data class DocumentProvider(val info: DocumentProviderInfo, val roots: List<DocumentProviderRoot>)

/** Info for a document provider to display to the user. */
data class DocumentProviderInfo(val title: String, val authority: String, val iconResource: Int)

/** Root with in a document provider (usually corresponds to an account). */
data class DocumentProviderRoot(val summary: String, val uri: Uri)

/** Returns true if the given document uri is a local device storage file. */
fun isLocalFile(uri: Uri): Boolean =
    uri.authority == DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY ||
        uri.authority == DOWNLOADS_AUTHORITY
