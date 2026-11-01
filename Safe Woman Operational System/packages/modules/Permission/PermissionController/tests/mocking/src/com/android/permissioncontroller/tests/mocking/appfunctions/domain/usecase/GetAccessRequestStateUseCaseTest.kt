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

import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_GRANTED
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAccessRequestStateUseCase
import com.android.permissioncontroller.tests.mocking.appfunctions.data.repository.FakeAppFunctionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

// TODO(b/424004217): Update this to the correct version code
/** Unit tests for [GetAccessRequestStateUseCase]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class GetAccessRequestStateUseCaseTest {
    @Test
    fun getAccessRequestStates_forAgent() = runTest {
        val repository = FakeAppFunctionRepository(accessFlags = accessFlags)
        val useCase = GetAccessRequestStateUseCase(repository)

        assertThat(
                useCase(
                    TEST_AGENT_PACKAGE_NAME,
                    listOf(
                        TEST_TARGET_PACKAGE_NAME,
                        TEST_TARGET_PACKAGE_NAME2,
                        TEST_TARGET_PACKAGE_NAME3,
                    ),
                )
            )
            .containsExactlyEntriesIn(
                mapOf(TEST_TARGET_PACKAGE_NAME to true, TEST_TARGET_PACKAGE_NAME2 to false)
            )

        assertThat(
                useCase(
                    TEST_AGENT_PACKAGE_NAME2,
                    listOf(
                        TEST_TARGET_PACKAGE_NAME,
                        TEST_TARGET_PACKAGE_NAME2,
                        TEST_TARGET_PACKAGE_NAME3,
                    ),
                )
            )
            .containsExactlyEntriesIn(
                mapOf(TEST_TARGET_PACKAGE_NAME to false, TEST_TARGET_PACKAGE_NAME2 to true)
            )
    }

    @Test
    fun getAccessRequestStates_forTarget() = runTest {
        val repository = FakeAppFunctionRepository(accessFlags = accessFlags)
        val useCase = GetAccessRequestStateUseCase(repository)

        assertThat(
                useCase(
                    listOf(TEST_AGENT_PACKAGE_NAME, TEST_AGENT_PACKAGE_NAME2),
                    TEST_TARGET_PACKAGE_NAME,
                )
            )
            .containsExactlyEntriesIn(
                mapOf(TEST_AGENT_PACKAGE_NAME to true, TEST_AGENT_PACKAGE_NAME2 to false)
            )

        assertThat(
                useCase(
                    listOf(TEST_AGENT_PACKAGE_NAME, TEST_AGENT_PACKAGE_NAME2),
                    TEST_TARGET_PACKAGE_NAME2,
                )
            )
            .containsExactlyEntriesIn(
                mapOf(TEST_AGENT_PACKAGE_NAME to false, TEST_AGENT_PACKAGE_NAME2 to true)
            )
    }

    @Test
    fun getAccessRequestStates_unrequestableFilteredOut() = runTest {
        val repository = FakeAppFunctionRepository(accessFlags = accessFlags)
        val useCase = GetAccessRequestStateUseCase(repository)

        assertThat(
                useCase(
                    listOf(TEST_AGENT_PACKAGE_NAME, TEST_AGENT_PACKAGE_NAME2),
                    TEST_TARGET_PACKAGE_NAME3,
                )
            )
            .isEmpty()
    }

    companion object {
        private const val TEST_AGENT_PACKAGE_NAME = "test.agent.package"
        private const val TEST_AGENT_PACKAGE_NAME2 = "test.agent.package2"
        private const val TEST_TARGET_PACKAGE_NAME = "test.target.package"
        private const val TEST_TARGET_PACKAGE_NAME2 = "test.target.package2"
        private const val TEST_TARGET_PACKAGE_NAME3 = "test.target.package3"
        private val accessFlags =
            mapOf(
                (TEST_AGENT_PACKAGE_NAME to TEST_TARGET_PACKAGE_NAME) to ACCESS_FLAG_USER_GRANTED,
                (TEST_AGENT_PACKAGE_NAME to TEST_TARGET_PACKAGE_NAME2) to ACCESS_FLAG_USER_DENIED,
                (TEST_AGENT_PACKAGE_NAME2 to TEST_TARGET_PACKAGE_NAME) to ACCESS_FLAG_USER_DENIED,
                (TEST_AGENT_PACKAGE_NAME2 to TEST_TARGET_PACKAGE_NAME2) to ACCESS_FLAG_USER_GRANTED,
            )
    }
}
