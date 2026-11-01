/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.permissioncontroller.appinteraction.data.repository

import android.app.AppInteractionContract
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.UserHandle
import com.android.permissioncontroller.appinteraction.domain.model.v37.AccessHistory

/** The data repository layer for app interaction data. See also [AppInteractionContract]. */
interface AppInteractionRepository {
    /**
     * Returns the access history Uri from the App Function table for the user in the context. See
     * [AppInteractionContract#getInteractionHistoryUriAsUser] for more details.
     */
    fun getInteractionHistoryUriAsUser(userHandle: UserHandle): Uri

    /** Returns a list of package names of device assistance apps. */
    fun getDeviceAssistancePackageNames(context: Context): List<String>

    suspend fun getAccessHistory(context: Context, userHandle: UserHandle): List<AccessHistory>

    companion object {
        @Volatile private var instance: AppInteractionRepository? = null

        /** Returns the singleton instance of [AppInteractionRepository]. */
        fun getInstance(): AppInteractionRepository =
            instance ?: synchronized(this) { AppInteractionRepositoryImpl().also { instance = it } }
    }
}

/** Implementation of [AppInteractionRepository]. */
class AppInteractionRepositoryImpl : AppInteractionRepository {
    override fun getInteractionHistoryUriAsUser(userHandle: UserHandle): Uri =
        AppInteractionContract.getInteractionHistoryUriAsUser(userHandle)

    override fun getDeviceAssistancePackageNames(context: Context): List<String> =
        AppInteractionContract.getDeviceAssistancePackageNames(context)

    override suspend fun getAccessHistory(
        context: Context,
        userHandle: UserHandle,
    ): List<AccessHistory> {
        val uri = getInteractionHistoryUriAsUser(userHandle)
        return queryAccessHistory(context.contentResolver, uri)
    }

    private fun queryAccessHistory(
        contentResolver: ContentResolver,
        uri: Uri,
    ): List<AccessHistory> {
        return contentResolver.query(uri, null, null, null)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(createAccessHistory(cursor))
                }
            }
        } ?: emptyList()
    }

    companion object {
        fun createAccessHistory(cursor: Cursor): AccessHistory {
            val targetPackageName =
                cursor.requireStringOrThrow(AppInteractionContract.COLUMN_TARGET_PACKAGE_NAME)

            return AccessHistory(
                agentPackageName =
                    cursor.requireStringOrThrow(AppInteractionContract.COLUMN_AGENT_PACKAGE_NAME),
                targetPackageName = targetPackageName,
                interactionType =
                    cursor.getIntOrThrow(AppInteractionContract.COLUMN_INTERACTION_TYPE),
                customInteractionType =
                    cursor.getStringOrThrow(AppInteractionContract.COLUMN_CUSTOM_INTERACTION_TYPE),
                interactionUri =
                    cursor.getStringOrThrow(AppInteractionContract.COLUMN_INTERACTION_URI),
                accessTime = cursor.requireLongOrThrow(AppInteractionContract.COLUMN_ACCESS_TIME),
            )
        }
    }
}
