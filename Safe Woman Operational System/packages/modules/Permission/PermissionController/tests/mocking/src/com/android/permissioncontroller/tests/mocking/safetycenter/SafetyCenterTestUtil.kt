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

package com.android.permissioncontroller.tests.mocking.safetycenter

import android.os.UserHandle
import android.permission.flags.Flags
import android.safetycenter.SafetyCenterIssue
import com.android.modules.utils.build.SdkLevel

object SafetyCenterTestUtil {

    fun createSafetyCenterIssueBuilder(
        id: String,
        title: CharSequence,
        summary: CharSequence,
        user: UserHandle,
        safetySourceIds: Set<String>,
        issueTypeId: String,
        safetySourceIssueId: String,
    ): SafetyCenterIssue.Builder =
        if (SdkLevel.isAtLeastB() && Flags.openSafetyCenterApis()) {
            SafetyCenterIssue.Builder(
                id,
                title,
                summary,
                user,
                safetySourceIds,
                issueTypeId,
                safetySourceIssueId,
            )
        } else {
            SafetyCenterIssue.Builder(id, title, summary)
        }
}
