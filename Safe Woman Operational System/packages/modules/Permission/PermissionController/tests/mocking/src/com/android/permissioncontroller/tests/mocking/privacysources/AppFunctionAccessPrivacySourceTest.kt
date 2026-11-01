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
package com.android.permissioncontroller.tests.mocking.privacysources

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.appfunctions.AppFunctionManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.os.Build
import android.platform.test.annotations.RequiresFlagsDisabled
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.provider.DeviceConfig
import android.safetycenter.SafetyCenterManager
import android.safetycenter.SafetyCenterManager.ACTION_REFRESH_SAFETY_SOURCES
import android.safetycenter.SafetyCenterManager.EXTRA_REFRESH_SAFETY_SOURCES_BROADCAST_ID
import android.safetycenter.SafetyEvent
import android.safetycenter.SafetyEvent.SAFETY_EVENT_TYPE_DEVICE_REBOOTED
import android.safetycenter.SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED
import android.safetycenter.SafetySourceData
import android.safetycenter.SafetySourceStatus
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.permission.utils.Utils
import com.android.permissioncontroller.privacysources.SafetyCenterReceiver.RefreshEvent.EVENT_DEVICE_REBOOTED
import com.android.permissioncontroller.privacysources.SafetyCenterReceiver.RefreshEvent.EVENT_REFRESH_REQUESTED
import com.android.permissioncontroller.privacysources.v37.AppFunctionAccessPrivacySource
import com.android.permissioncontroller.privacysources.v37.AppFunctionAccessPrivacySource.Companion.APP_FUNCTION_ACCESS_SOURCE_ID
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

/** Tests for [AppFunctionAccessPrivacySource]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class AppFunctionAccessPrivacySourceTest {
    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    private lateinit var mockitoSession: MockitoSession
    private lateinit var appFunctionAccessPrivacySource: AppFunctionAccessPrivacySource
    @Mock lateinit var mockSafetyCenterManager: SafetyCenterManager

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockitoSession =
            ExtendedMockito.mockitoSession()
                .mockStatic(DeviceConfig::class.java)
                .mockStatic(PermissionControllerApplication::class.java)
                .mockStatic(Utils::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()
        `when`(
                Utils.getSystemServiceSafe(
                    any(ContextWrapper::class.java),
                    eq(SafetyCenterManager::class.java),
                )
            )
            .thenReturn(mockSafetyCenterManager)

        appFunctionAccessPrivacySource = AppFunctionAccessPrivacySource()
    }

    @After
    fun cleanup() {
        mockitoSession.finishMocking()
    }

    @Test
    fun safetyCenterEnabledChanged_enabled_doesNothing() {
        appFunctionAccessPrivacySource.safetyCenterEnabledChanged(context, true)

        verifyNoMoreInteractions(mockSafetyCenterManager)
    }

    @Test
    fun safetyCenterEnabledChanged_disabled_doesNothing() {
        appFunctionAccessPrivacySource.safetyCenterEnabledChanged(context, false)

        verifyNoMoreInteractions(mockSafetyCenterManager)
    }

    @RequiresFlagsEnabled(FLAG_APP_FUNCTION_ACCESS_UI_ENABLED)
    @Test
    fun rescanAndPushSafetyCenterData_refreshRequested_appFunctionsEnabled_setsDataWithStatus() {
        val refreshIntent =
            Intent(ACTION_REFRESH_SAFETY_SOURCES)
                .putExtra(EXTRA_REFRESH_SAFETY_SOURCES_BROADCAST_ID, REFRESH_ID)

        appFunctionAccessPrivacySource.rescanAndPushSafetyCenterData(
            context,
            refreshIntent,
            EVENT_REFRESH_REQUESTED,
        )

        val expectedSafetySourceData: SafetySourceData =
            SafetySourceData.Builder()
                .setStatus(
                    SafetySourceStatus.Builder(
                            APP_FUNCTION_ACCESS_TITLE,
                            APP_FUNCTION_ACCESS_SUMMARY,
                            SafetySourceData.SEVERITY_LEVEL_UNSPECIFIED,
                        )
                        .setPendingIntent(
                            PendingIntent.getActivity(
                                context,
                                /* requestCode= */ 0,
                                Intent(AppFunctionManager.ACTION_MANAGE_APP_FUNCTION_ACCESS),
                                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                            )
                        )
                        .build()
                )
                .build()
        val expectedSafetyEvent =
            SafetyEvent.Builder(SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId(AppDataSharingUpdatesPrivacySourceTest.REFRESH_ID)
                .build()
        verify(mockSafetyCenterManager)
            .setSafetySourceData(
                APP_FUNCTION_ACCESS_SOURCE_ID,
                expectedSafetySourceData,
                expectedSafetyEvent,
            )
    }

    @RequiresFlagsEnabled(FLAG_APP_FUNCTION_ACCESS_UI_ENABLED)
    @Test
    fun rescanAndPushSafetyCenterData_deviceRebooted_appFunctionsEnabled_setsDataWithStatus() {
        val bootCompleteIntent = Intent(ACTION_BOOT_COMPLETED)

        appFunctionAccessPrivacySource.rescanAndPushSafetyCenterData(
            AppDataSharingUpdatesPrivacySourceTest.context,
            bootCompleteIntent,
            EVENT_DEVICE_REBOOTED,
        )

        val expectedSafetySourceData: SafetySourceData =
            SafetySourceData.Builder()
                .setStatus(
                    SafetySourceStatus.Builder(
                            APP_FUNCTION_ACCESS_TITLE,
                            APP_FUNCTION_ACCESS_SUMMARY,
                            SafetySourceData.SEVERITY_LEVEL_UNSPECIFIED,
                        )
                        .setPendingIntent(
                            PendingIntent.getActivity(
                                context,
                                /* requestCode= */ 0,
                                Intent(AppFunctionManager.ACTION_MANAGE_APP_FUNCTION_ACCESS),
                                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                            )
                        )
                        .build()
                )
                .build()
        val expectedSafetyEvent = SafetyEvent.Builder(SAFETY_EVENT_TYPE_DEVICE_REBOOTED).build()
        verify(mockSafetyCenterManager)
            .setSafetySourceData(
                APP_FUNCTION_ACCESS_SOURCE_ID,
                expectedSafetySourceData,
                expectedSafetyEvent,
            )
    }

    @RequiresFlagsDisabled(FLAG_APP_FUNCTION_ACCESS_UI_ENABLED)
    @Test
    fun rescanAndPushSafetyCenterData_refreshRequested_appFunctionsDisabled_setsNullData() {
        val refreshIntent =
            Intent(ACTION_REFRESH_SAFETY_SOURCES)
                .putExtra(EXTRA_REFRESH_SAFETY_SOURCES_BROADCAST_ID, REFRESH_ID)

        appFunctionAccessPrivacySource.rescanAndPushSafetyCenterData(
            context,
            refreshIntent,
            EVENT_REFRESH_REQUESTED,
        )

        val expectedSafetyEvent =
            SafetyEvent.Builder(SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId(REFRESH_ID)
                .build()
        verify(mockSafetyCenterManager)
            .setSafetySourceData(APP_FUNCTION_ACCESS_SOURCE_ID, null, expectedSafetyEvent)
    }

    @RequiresFlagsDisabled(FLAG_APP_FUNCTION_ACCESS_UI_ENABLED)
    @Test
    fun rescanAndPushSafetyCenterData_deviceRebooted_appFunctionsDisabled_setsNullData() {
        val bootCompleteIntent = Intent(ACTION_BOOT_COMPLETED)

        appFunctionAccessPrivacySource.rescanAndPushSafetyCenterData(
            AppDataSharingUpdatesPrivacySourceTest.context,
            bootCompleteIntent,
            EVENT_DEVICE_REBOOTED,
        )

        val expectedSafetyEvent = SafetyEvent.Builder(SAFETY_EVENT_TYPE_DEVICE_REBOOTED).build()
        verify(mockSafetyCenterManager)
            .setSafetySourceData(APP_FUNCTION_ACCESS_SOURCE_ID, null, expectedSafetyEvent)
    }

    /** Companion object for [AppFunctionAccessPrivacySourceTest]. */
    companion object {
        // Flag lib changes has caused issues with jarjar and now annotations require the jarjar
        // package prepended to the flag string
        const val FLAG_APP_FUNCTION_ACCESS_UI_ENABLED =
            "com.android.permissioncontroller.jarjar.android.permission.flags.app_function_access_ui_enabled"

        // Real context, used in order to avoid mocking resources.
        var context: Context = ApplicationProvider.getApplicationContext()
        const val APP_FUNCTION_ACCESS_TITLE: String = "Agents"
        const val APP_FUNCTION_ACCESS_SUMMARY: String =
            "Agents that can access info or take actions in apps"
        const val REFRESH_ID: String = "refresh_id"
    }
}
