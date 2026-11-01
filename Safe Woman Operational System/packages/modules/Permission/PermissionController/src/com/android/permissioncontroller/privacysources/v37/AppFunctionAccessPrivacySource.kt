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
package com.android.permissioncontroller.privacysources.v37

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.appfunctions.AppFunctionManager
import android.content.Context
import android.content.Intent
import android.permission.flags.Flags
import android.safetycenter.SafetyCenterManager
import android.safetycenter.SafetyEvent
import android.safetycenter.SafetySourceData
import android.safetycenter.SafetySourceStatus
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.utils.Utils
import com.android.permissioncontroller.privacysources.PrivacySource
import com.android.permissioncontroller.privacysources.SafetyCenterReceiver.RefreshEvent

/**
 * Privacy source providing the App Functions Access page entry to Safety Center.
 *
 * The content of the App Functions Access page is static, however the entry should only be
 * displayed if the App Functions Access feature is enabled.
 */
class AppFunctionAccessPrivacySource : PrivacySource {
    override val shouldProcessProfileRequest: Boolean = false

    override fun safetyCenterEnabledChanged(context: Context, enabled: Boolean) {
        // Do nothing
    }

    override fun rescanAndPushSafetyCenterData(
        context: Context,
        intent: Intent,
        refreshEvent: RefreshEvent,
    ) {
        val safetyCenterManager: SafetyCenterManager =
            Utils.getSystemServiceSafe(context, SafetyCenterManager::class.java)

        val safetySourceData =
            if (Flags.appFunctionAccessUiEnabled()) {
                val pendingIntent = getPendingIntentForAppFunctionAgentList(context)
                val status =
                    SafetySourceStatus.Builder(
                            context.getString(R.string.app_function_access_settings_title),
                            context.getString(R.string.app_function_access_settings_summary),
                            SafetySourceData.SEVERITY_LEVEL_UNSPECIFIED,
                        )
                        .setPendingIntent(pendingIntent)
                        .build()
                SafetySourceData.Builder().setStatus(status).build()
            } else {
                null
            }

        safetyCenterManager.setSafetySourceData(
            APP_FUNCTION_ACCESS_SOURCE_ID,
            safetySourceData,
            createSafetyEvent(refreshEvent, intent),
        )
    }

    /** Companion object for [AppFunctionAccessPrivacySource]. */
    companion object {
        /** Source id for safety center source for app data sharing updates. */
        const val APP_FUNCTION_ACCESS_SOURCE_ID = "AndroidAppFunctionAccess"

        private fun getPendingIntentForAppFunctionAgentList(context: Context): PendingIntent {
            val intent = Intent(AppFunctionManager.ACTION_MANAGE_APP_FUNCTION_ACCESS)
            return PendingIntent.getActivity(
                context,
                /* requestCode= */ 0,
                intent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
            )
        }

        private fun createSafetyEvent(refreshEvent: RefreshEvent, intent: Intent): SafetyEvent {
            return when (refreshEvent) {
                RefreshEvent.EVENT_REFRESH_REQUESTED -> {
                    val refreshBroadcastId =
                        intent.getStringExtra(
                            SafetyCenterManager.EXTRA_REFRESH_SAFETY_SOURCES_BROADCAST_ID
                        )
                    SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                        .setRefreshBroadcastId(refreshBroadcastId)
                        .build()
                }
                RefreshEvent.EVENT_DEVICE_REBOOTED -> {
                    SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_DEVICE_REBOOTED).build()
                }
                RefreshEvent.UNKNOWN -> {
                    SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_SOURCE_STATE_CHANGED).build()
                }
            }
        }
    }
}
