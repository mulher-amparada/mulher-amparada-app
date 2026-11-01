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

import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_OTHER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_OTHER_GRANTED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_GRANTED
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.permissioncontroller.appfunctions.domain.usecase.UpdateAccessUseCase
import com.android.permissioncontroller.tests.mocking.appfunctions.data.repository.FakeAppFunctionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

// TODO(b/424004217): Update this to the correct version code
/** Unit tests for [UpdateAccessUseCase]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class UpdateAccessUseCaseTest {
    @Test
    fun updateAccess() = runTest {
        val repository = FakeAppFunctionRepository()
        val useCase = UpdateAccessUseCase(repository)
        repository.updateAccessFlags(
            TEST_AGENT_PACKAGE_NAME,
            TEST_TARGET_PACKAGE_NAME,
            FLAG_MASK,
            ACCESS_FLAG_OTHER_DENIED,
        )

        useCase(TEST_AGENT_PACKAGE_NAME, TEST_TARGET_PACKAGE_NAME, true)
        assertThat(
                repository.getAccessFlags(TEST_AGENT_PACKAGE_NAME, TEST_TARGET_PACKAGE_NAME) and
                    FLAG_MASK
            )
            .isEqualTo(ACCESS_FLAG_USER_GRANTED)

        useCase(TEST_AGENT_PACKAGE_NAME, TEST_TARGET_PACKAGE_NAME, false)
        assertThat(
                repository.getAccessFlags(TEST_AGENT_PACKAGE_NAME, TEST_TARGET_PACKAGE_NAME) and
                    FLAG_MASK
            )
            .isEqualTo(ACCESS_FLAG_USER_DENIED)
    }

    companion object {
        private const val TEST_AGENT_PACKAGE_NAME = "test.agent.package"
        private const val TEST_TARGET_PACKAGE_NAME = "test.target.package"
        private const val FLAG_MASK =
            ACCESS_FLAG_USER_GRANTED or
                ACCESS_FLAG_USER_DENIED or
                ACCESS_FLAG_OTHER_GRANTED or
                ACCESS_FLAG_OTHER_DENIED
    }
}
