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

package com.android.permissioncontroller.tests.mocking.appfunctions.domain.usecase

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.domain.model.v31.AppFunctionPackageInfo
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

// TODO(b/424004217): Update this to the correct version code
/** Unit tests for [GetAppFunctionPackageInfoUseCaseImpl]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class GetAppFunctionPackageInfoUseCaseTest {
    @Mock private lateinit var application: PermissionControllerApplication
    @Mock private lateinit var packageIcon: Drawable
    @Mock private lateinit var deviceSettingsIcon: Drawable
    @Mock private lateinit var fallbackDeviceSettingsIcon: Drawable

    private lateinit var packagesAndIcons: Map<String, Drawable>

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
        whenever(application.getString(R.string.app_function_device_settings_target_title))
            .thenReturn(DEVICE_SETTINGS_LABEL)
        whenever(application.getDrawable(R.drawable.ic_appfunction_target_device_settings))
            .thenReturn(fallbackDeviceSettingsIcon)

        packagesAndIcons =
            mapOf(
                TEST_PACKAGE_NAME to packageIcon,
                DEVICE_SETTINGS_PACKAGE_NAME to deviceSettingsIcon,
            )
    }

    @After
    fun finish() {
        mockitoSession?.finishMocking()
    }

    @Test
    fun getAppFunctionPackageInfo_returnsAppFunctionPackageInfo() = runTest {
        val expectedPackageInfo = AppFunctionPackageInfo(TEST_PACKAGE_NAME, TEST_LABEL, packageIcon)
        val useCase =
            GetAppFunctionPackageInfoUseCaseImpl(
                FakePackageRepository(
                    packagesAndLabels = packagesAndLabels,
                    packageIcons = packagesAndIcons,
                )
            )
        val actualPackageInfo = useCase(TEST_PACKAGE_NAME, application, Process.myUserHandle())
        assertThat(actualPackageInfo).isEqualTo(expectedPackageInfo)
    }

    @Test
    fun getAppFunctionPackageInfo_returnsDeviceSettingsPackageInfo() = runTest {
        val expectedPackageInfo =
            AppFunctionPackageInfo(
                DEVICE_SETTINGS_PACKAGE_NAME,
                DEVICE_SETTINGS_LABEL,
                deviceSettingsIcon,
            )
        val useCase =
            GetAppFunctionPackageInfoUseCaseImpl(
                FakePackageRepository(
                    packagesAndLabels = packagesAndLabels,
                    packageIcons = packagesAndIcons,
                    settingsPackageName = DEVICE_SETTINGS_PACKAGE_NAME,
                )
            )
        val actualPackageInfo =
            useCase(DEVICE_SETTINGS_PACKAGE_NAME, application, Process.myUserHandle())
        assertThat(actualPackageInfo).isEqualTo(expectedPackageInfo)
    }

    @Test
    fun getAppFunctionPackageInfo_getSettingsPackageNameNull_returnsDeviceSettingsPackageInfo() =
        runTest {
            val expectedPackageInfo =
                AppFunctionPackageInfo(
                    DEVICE_SETTINGS_PACKAGE_NAME,
                    DEVICE_SETTINGS_LABEL,
                    fallbackDeviceSettingsIcon,
                )
            val useCase =
                GetAppFunctionPackageInfoUseCaseImpl(
                    FakePackageRepository(
                        packagesAndLabels = packagesAndLabels,
                        packageIcons = packagesAndIcons,
                    )
                )
            val actualPackageInfo =
                useCase(DEVICE_SETTINGS_PACKAGE_NAME, application, Process.myUserHandle())
            assertThat(actualPackageInfo).isEqualTo(expectedPackageInfo)
        }

    @Test
    fun getAppFunctionPackageInfo_getSettingsPackageIconNull_returnsDeviceSettingsPackageInfo() =
        runTest {
            val expectedPackageInfo =
                AppFunctionPackageInfo(
                    DEVICE_SETTINGS_PACKAGE_NAME,
                    DEVICE_SETTINGS_LABEL,
                    fallbackDeviceSettingsIcon,
                )
            val useCase =
                GetAppFunctionPackageInfoUseCaseImpl(
                    FakePackageRepository(
                        packagesAndLabels = packagesAndLabels,
                        settingsPackageName = DEVICE_SETTINGS_PACKAGE_NAME,
                    )
                )
            val actualPackageInfo =
                useCase(DEVICE_SETTINGS_PACKAGE_NAME, application, Process.myUserHandle())
            assertThat(actualPackageInfo).isEqualTo(expectedPackageInfo)
        }

    companion object {
        private const val TEST_PACKAGE_NAME = "test.agent.package"
        private const val TEST_LABEL = "Test Agent"
        private const val DEVICE_SETTINGS_PACKAGE_NAME = "android"
        private const val DEVICE_SETTINGS_LABEL = "Device Settings"
        private val packagesAndLabels = mapOf(TEST_PACKAGE_NAME to TEST_LABEL)
    }
}
