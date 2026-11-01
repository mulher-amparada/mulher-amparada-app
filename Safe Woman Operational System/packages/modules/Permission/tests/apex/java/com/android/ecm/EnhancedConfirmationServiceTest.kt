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

package com.android.ecm

import android.Manifest.permission.RECEIVE_SMS
import android.Manifest.permission.SEND_SMS
import android.app.role.RoleManager.ROLE_DIALER
import android.app.role.RoleManager.ROLE_SMS
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession
import com.android.server.LocalManagerRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.VANILLA_ICE_CREAM, codeName = "VanillaIceCream")
class EnhancedConfirmationServiceTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var mockitoSession: MockitoSession
    private lateinit var enhancedConfirmationService: EnhancedConfirmationService

    private lateinit var defaultCallRestrictedSettings: Set<String>

    @Mock private lateinit var mockContext: Context

    @Mock private lateinit var mockResources: Resources

    @Before
    fun setUp() {
        initMocks(this)
        mockitoSession =
            mockitoSession()
                .mockStatic(LocalManagerRegistry::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()
        enhancedConfirmationService = EnhancedConfirmationService(context)
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, null)
        enhancedConfirmationService.initSettings(mockContext)
        defaultCallRestrictedSettings =
            enhancedConfirmationService.mUntrustedCallRestrictedSettings.toSet()
    }

    @After
    fun finishMockingApexEnvironment() {
        mockitoSession.finishMocking()
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, null)
        enhancedConfirmationService.initSettings(mockContext)
    }

    @Test
    fun exemptSettingsAreExempt() {
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, EXEMPT_SETTINGS)
        enhancedConfirmationService.loadPackageExemptSettings(mockContext)

        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(SEND_SMS))
            .isFalse()
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(RECEIVE_SMS))
            .isFalse()
    }

    @Test
    fun nonExemptSettingsAreNotExempt() {
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, EXEMPT_SETTINGS)
        enhancedConfirmationService.loadPackageExemptSettings(mockContext)

        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(ROLE_DIALER))
            .isTrue()
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(ROLE_SMS))
            .isTrue()
    }

    @Test
    fun givenNoExemptSettingsThenNoneExempt() {
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, null)
        enhancedConfirmationService.loadPackageExemptSettings(mockContext)

        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(SEND_SMS))
            .isTrue()
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(RECEIVE_SMS))
            .isTrue()
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(ROLE_DIALER))
            .isTrue()
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings.contains(ROLE_SMS))
            .isTrue()
    }

    @Test
    fun wildcardExemptsAllSettings() {
        overrideStringArrayResource(EXEMPT_SETTINGS_RESOURCE_NAME, EXEMPT_ALL_SETTINGS)
        enhancedConfirmationService.loadPackageExemptSettings(mockContext)
        assertThat(enhancedConfirmationService.mPerPackageProtectedSettings).isEmpty()
    }

    @Test
    fun exemptionDoesntAffectCallSettings() {
        overrideStringArrayResource(
            EXEMPT_SETTINGS_RESOURCE_NAME,
            defaultCallRestrictedSettings.toTypedArray(),
        )
        enhancedConfirmationService.loadPackageExemptSettings(mockContext)
        assertThat(enhancedConfirmationService.mUntrustedCallRestrictedSettings)
            .isEqualTo(defaultCallRestrictedSettings)
    }

    private fun overrideStringArrayResource(name: String, newValue: Array<String>?) {
        `when`(mockContext.getResources()).thenReturn(mockResources)
        `when`(mockResources.getIdentifier(Mockito.eq(name), Mockito.any(), Mockito.any()))
            .thenReturn(if (newValue == null) 0 else EXEMPT_SETTINGS_RESOURCE)
        if (newValue != null) {
            `when`(mockResources.getStringArray(Mockito.eq(EXEMPT_SETTINGS_RESOURCE)))
                .thenReturn(newValue)
        }
    }

    companion object {
        private const val EXEMPT_SETTINGS_RESOURCE = 1 // Fake resource id
        private const val EXEMPT_SETTINGS_RESOURCE_NAME =
            "config_enhancedConfirmationModeExemptSettings"

        private val EXEMPT_SETTINGS = arrayOf<String>(SEND_SMS, RECEIVE_SMS)

        private val EXEMPT_ALL_SETTINGS = arrayOf<String>("*")
    }
}
