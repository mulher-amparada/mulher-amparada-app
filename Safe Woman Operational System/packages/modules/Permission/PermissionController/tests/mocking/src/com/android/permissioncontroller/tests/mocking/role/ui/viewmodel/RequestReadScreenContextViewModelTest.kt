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
package com.android.permissioncontroller.tests.mocking.role.ui.viewmodel

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.MODE_IGNORED
import android.app.voiceinteraction.VoiceInteractionManager
import android.os.Build
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.role.ui.v37.RequestReadScreenContextViewModel
import com.android.permissioncontroller.tests.mocking.appops.data.repository.FakeAppOpRepository
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

/** Unit tests for [RequestReadScreenContextViewModel] */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
@RequiresFlagsEnabled(
    RequestReadScreenContextViewModelTest.FLAG_ASSIST_SETTINGS_PRIVACY_IMPROVEMENTS_ENABLED
)
@RunWith(AndroidJUnit4::class)
class RequestReadScreenContextViewModelTest {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Mock private lateinit var application: PermissionControllerApplication

    private val appOpsRepository = FakeAppOpRepository(emptyFlow())

    private var mockitoSession: MockitoSession? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockitoSession =
            ExtendedMockito.mockitoSession()
                .mockStatic(PermissionControllerApplication::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()
        ExtendedMockito.doReturn(application).`when` { PermissionControllerApplication.get() }
        whenever(application.applicationContext).thenReturn(application)
    }

    @After
    fun finish() {
        mockitoSession?.finishMocking()
    }

    @Test
    fun setAllowed() = runTest {
        appOpsRepository.setUidMode(
            AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
            PACKAGE_UID,
            MODE_IGNORED,
        )

        val viewModel = getViewModel()
        viewModel.setAllowed()
        runCurrent()

        assertThat(
                appOpsRepository.checkOpNoThrow(
                    AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                    PACKAGE_UID,
                    PACKAGE_NAME,
                )
            )
            .isEqualTo(MODE_ALLOWED)
    }

    @Test
    fun markRequestDenied() = runTest {
        appOpsRepository.setUidMode(
            AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
            PACKAGE_UID,
            MODE_IGNORED,
        )

        val viewModel = getViewModel()
        viewModel.markRequestDenied()
        runCurrent()

        assertThat(
                appOpsRepository.checkOpNoThrow(
                    AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                    PACKAGE_UID,
                    PACKAGE_NAME,
                )
            )
            .isEqualTo(MODE_IGNORED)
    }

    private fun TestScope.getViewModel(): RequestReadScreenContextViewModel {
        return RequestReadScreenContextViewModel(
            application,
            PACKAGE_UID,
            appOpsRepository,
            mock(VoiceInteractionManager::class.java),
            backgroundScope,
            StandardTestDispatcher(testScheduler),
        )
    }

    companion object {
        // Flag lib changes has caused issues with jarjar and now annotations require the jarjar
        // package prepended to the flag string
        const val FLAG_ASSIST_SETTINGS_PRIVACY_IMPROVEMENTS_ENABLED =
            "com.android.permissioncontroller.jarjar.android.permission.flags." +
                "assist_settings_privacy_improvements_enabled"

        private const val PACKAGE_NAME = "test.assistant.package"
        private const val PACKAGE_UID = FakePackageRepository.TEST_UID
    }
}
