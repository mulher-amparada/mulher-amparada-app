/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.permissioncontroller.tests.mocking.safetycenter.ui.model

import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Bundle
import android.os.UserHandle
import android.safetycenter.SafetyCenterData
import android.safetycenter.SafetyCenterEntryGroup
import android.safetycenter.SafetyCenterEntryOrGroup
import android.safetycenter.SafetyCenterIssue
import android.safetycenter.SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING
import android.safetycenter.SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK
import android.safetycenter.SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_RECOMMENDATION
import android.safetycenter.SafetyCenterStatus
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.permissioncontroller.safetycenter.ui.model.ActionId
import com.android.permissioncontroller.safetycenter.ui.model.IssueId
import com.android.permissioncontroller.safetycenter.ui.model.IssueUiData
import com.android.permissioncontroller.safetycenter.ui.model.SafetyCenterUiData
import com.android.permissioncontroller.tests.mocking.safetycenter.SafetyCenterTestUtil.createSafetyCenterIssueBuilder
import com.android.safetycenter.internaldata.SafetyCenterBundles.ISSUES_TO_GROUPS_BUNDLE_KEY
import com.android.safetycenter.internaldata.SafetyCenterIds
import com.android.safetycenter.internaldata.SafetyCenterIssueId
import com.android.safetycenter.internaldata.SafetyCenterIssueKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE, codeName = "UpsideDownCake")
class SafetyCenterUiDataTest {

    @Test
    fun getMatchingGroup_validMatchingGroup_returnsExpectedEntryGroup() {
        val matchingGroup = entryGroup(MATCHING_GROUP_ID)
        val nonMatchingGroup = entryGroup(NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(entryGroups = listOf(matchingGroup, nonMatchingGroup))

        val result = uiData(safetyCenterData).getMatchingGroup(MATCHING_GROUP_ID)

        assertThat(result).isEqualTo(matchingGroup)
    }

    @Test
    fun getMatchingGroup_noMatchingGroup_returnsNull() {
        val nonMatchingGroup = entryGroup(NON_MATCHING_GROUP_ID)
        val safetyCenterData = createSafetyCenterData(entryGroups = listOf(nonMatchingGroup))

        val result = uiData(safetyCenterData).getMatchingGroup(MATCHING_GROUP_ID)

        assertThat(result).isNull()
    }

    @Test
    fun getMatchingIssues_defaultMatchingIssue_noExtras_returnsListOfIssues() {
        val defaultMatchingIssue = issue("id1", MATCHING_GROUP_ID)
        val nonMatchingIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(issues = listOf(defaultMatchingIssue, nonMatchingIssue))

        val result = uiData(safetyCenterData).getMatchingIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(defaultMatchingIssue)
    }

    @Test
    fun getMatchingIssues_defaultMatchingIssue_unrelatedExtras_returnsListOfIssues() {
        val defaultMatchingIssue = issue("id1", MATCHING_GROUP_ID)
        val nonMatchingIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                issues = listOf(defaultMatchingIssue, nonMatchingIssue),
                extras =
                    createSafetyCenterExtras(
                        Bundle().apply {
                            putStringArrayList(
                                nonMatchingIssue.id,
                                arrayListOf(NON_MATCHING_GROUP_ID),
                            )
                        }
                    ),
            )

        val result = uiData(safetyCenterData).getMatchingIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(defaultMatchingIssue)
    }

    @Test
    fun getMatchingIssues_mappingMatchingIssue_returnsListOfIssues() {
        val mappingMatchingIssue = issue("id1", NON_MATCHING_GROUP_ID)
        val nonMatchingIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                issues = listOf(mappingMatchingIssue, nonMatchingIssue),
                extras =
                    createSafetyCenterExtras(
                        Bundle().apply {
                            putStringArrayList(
                                mappingMatchingIssue.id,
                                arrayListOf(MATCHING_GROUP_ID),
                            )
                        }
                    ),
            )

        val result = uiData(safetyCenterData).getMatchingIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(mappingMatchingIssue)
    }

    @Test
    fun getMatchingIssues_noDefaultMatchingIssue_returnsEmptyList() {
        val nonMatchingIssue = issue("id1", NON_MATCHING_GROUP_ID)
        val dismissedIssue = issue("id2", MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                issues = listOf(nonMatchingIssue),
                dismissedIssues = listOf(dismissedIssue),
            )

        val result = uiData(safetyCenterData).getMatchingIssues(MATCHING_GROUP_ID)

        assertThat(result).isEmpty()
    }

    @Test
    fun getMatchingDismissedIssues_defaultMatchingDismissedIssue_returnsListOfDismissedIssues() {
        val defaultMatchingDismissedIssue = issue("id1", MATCHING_GROUP_ID)
        val nonMatchingDismissedIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                dismissedIssues = listOf(defaultMatchingDismissedIssue, nonMatchingDismissedIssue)
            )

        val result = uiData(safetyCenterData).getMatchingDismissedIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(defaultMatchingDismissedIssue)
    }

    @Test
    fun getMatchingDismissedIssues_defaultMatchingDismissedIssue2_returnsListOfDismissedIssues() {
        val defaultMatchingDismissedIssue = issue("id1", MATCHING_GROUP_ID)
        val nonMatchingDismissedIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                dismissedIssues = listOf(defaultMatchingDismissedIssue, nonMatchingDismissedIssue),
                extras =
                    createSafetyCenterExtras(
                        Bundle().apply {
                            putStringArrayList(
                                nonMatchingDismissedIssue.id,
                                arrayListOf(NON_MATCHING_GROUP_ID),
                            )
                        }
                    ),
            )

        val result = uiData(safetyCenterData).getMatchingDismissedIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(defaultMatchingDismissedIssue)
    }

    @Test
    fun getMatchingDismissedIssues_mappingMatchingDismissedIssue_returnsListOfDismissedIssues() {
        val mappingMatchingDismissedIssue = issue("id1", NON_MATCHING_GROUP_ID)
        val nonMatchingDismissedIssue = issue("id2", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                dismissedIssues = listOf(mappingMatchingDismissedIssue, nonMatchingDismissedIssue),
                extras =
                    createSafetyCenterExtras(
                        Bundle().apply {
                            putStringArrayList(
                                mappingMatchingDismissedIssue.id,
                                arrayListOf(MATCHING_GROUP_ID),
                            )
                        }
                    ),
            )

        val result = uiData(safetyCenterData).getMatchingDismissedIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(mappingMatchingDismissedIssue)
    }

    @Test
    fun getMatchingDismissedIssues_noDefaultMatchingDismissedIssue_returnsEmptyList() {
        val nonMatchingDismissedIssue = issue("id1", NON_MATCHING_GROUP_ID)
        val nonDismissedIssue = issue("id2", MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                issues = listOf(nonDismissedIssue),
                dismissedIssues = listOf(nonMatchingDismissedIssue),
            )

        val result = uiData(safetyCenterData).getMatchingDismissedIssues(MATCHING_GROUP_ID)

        assertThat(result).isEmpty()
    }

    @Test
    fun getMatchingDismissedIssues_doesntReturnGreenIssues() {
        val greenDismissedIssue =
            issue("id1", MATCHING_GROUP_ID, severityLevel = ISSUE_SEVERITY_LEVEL_OK)
        val yellowDismissedIssue =
            issue("id2", MATCHING_GROUP_ID, severityLevel = ISSUE_SEVERITY_LEVEL_RECOMMENDATION)
        val redDismissedIssue =
            issue("id3", MATCHING_GROUP_ID, severityLevel = ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING)
        val nonMatchingDismissedIssue = issue("id4", NON_MATCHING_GROUP_ID)
        val safetyCenterData =
            createSafetyCenterData(
                dismissedIssues =
                    listOf(
                        redDismissedIssue,
                        yellowDismissedIssue,
                        greenDismissedIssue,
                        nonMatchingDismissedIssue,
                    )
            )

        val result = uiData(safetyCenterData).getMatchingDismissedIssues(MATCHING_GROUP_ID)

        assertThat(result).containsExactly(redDismissedIssue, yellowDismissedIssue).inOrder()
    }

    @Test
    fun issueUiDatas_returnsIssueUiData() {
        val issue1 = issue("id1", "group1")
        val issue2 = issue("id2", "group2")
        val safetyCenterData = createSafetyCenterData(listOf(issue1, issue2))

        val result = uiData(safetyCenterData).issueUiDatas

        assertThat(result)
            .containsExactly(
                IssueUiData(issue1, isDismissed = false),
                IssueUiData(issue2, isDismissed = false),
            )
            .inOrder()
    }

    @Test
    fun issueUiDatas_withResolvedIssues_returnsExpectedIssueUiData() {
        val resolvedActionId = "actionId"
        val resolvedIssue = issue("resolvedId", "group1")
        val unresolvedIssue = issue("unresolvedId", "group2")
        val safetyCenterData = createSafetyCenterData(listOf(resolvedIssue, unresolvedIssue))

        val result =
            uiData(safetyCenterData, resolvedIssues = mapOf(resolvedIssue.id to resolvedActionId))
                .issueUiDatas

        assertThat(result[0].resolvedIssueActionId).isEqualTo(resolvedActionId)
        assertThat(result[1].resolvedIssueActionId).isNull()
    }

    @Test
    fun issueUiDatas_withSameTaskSourceId_returnsExpectedIssueUiData() {
        val taskId = 42
        val sameTaskSourceId = "sameTaskSourceId"
        val sameTaskIssue =
            issueWithEncodedId(
                encodeIssueId("sameTaskIssue", sourceId = sameTaskSourceId),
                "group1",
            )
        val differentTaskIssue = issue("differentTaskIssue", "group2")
        val safetyCenterData = createSafetyCenterData(listOf(sameTaskIssue, differentTaskIssue))

        val result =
            uiData(safetyCenterData, taskId, sameTaskSourceIds = listOf(sameTaskSourceId))
                .issueUiDatas

        assertThat(result[0].launchTaskId).isEqualTo(taskId)
        assertThat(result[1].launchTaskId).isNull()
    }

    private companion object {
        const val MATCHING_GROUP_ID = "matching_group_id"
        const val NON_MATCHING_GROUP_ID = "non_matching_group_id"

        private fun uiData(
            safetyCenterData: SafetyCenterData,
            taskId: Int = 0,
            sameTaskSourceIds: List<String> = emptyList(),
            resolvedIssues: Map<IssueId, ActionId> = emptyMap(),
        ) = SafetyCenterUiData(safetyCenterData, taskId, sameTaskSourceIds, resolvedIssues)

        fun createSafetyCenterData(
            issues: List<SafetyCenterIssue> = listOf(),
            entryGroups: List<SafetyCenterEntryGroup> = listOf(),
            dismissedIssues: List<SafetyCenterIssue> = listOf(),
            extras: Bundle = Bundle(),
        ): SafetyCenterData {
            val safetyCenterStatus =
                SafetyCenterStatus.Builder("status title", "status summary").build()
            val builder = SafetyCenterData.Builder(safetyCenterStatus)
            for (issue in issues) {
                builder.addIssue(issue)
            }
            for (group in entryGroups) {
                builder.addEntryOrGroup(SafetyCenterEntryOrGroup(group))
            }
            for (dismissedIssue in dismissedIssues) {
                builder.addDismissedIssue(dismissedIssue)
            }
            builder.setExtras(extras)
            return builder.build()
        }

        fun createSafetyCenterExtras(issuesToGroupsMapping: Bundle) =
            Bundle().apply { putBundle(ISSUES_TO_GROUPS_BUNDLE_KEY, issuesToGroupsMapping) }

        fun entryGroup(groupId: String) =
            SafetyCenterEntryGroup.Builder(groupId, "group title").build()

        fun issue(
            issueId: String,
            groupId: String,
            severityLevel: Int = ISSUE_SEVERITY_LEVEL_RECOMMENDATION,
        ): SafetyCenterIssue = issueWithEncodedId(encodeIssueId(issueId), groupId, severityLevel)

        private fun issueWithEncodedId(
            encodedIssueId: String,
            groupId: String,
            severityLevel: Int = ISSUE_SEVERITY_LEVEL_RECOMMENDATION,
        ) =
            createSafetyCenterIssueBuilder(
                    encodedIssueId,
                    "issue title",
                    "issue summary",
                    UserHandle.of(UserHandle.myUserId()),
                    setOf("defaultSourceId"),
                    "defaultIssueTypeId",
                    "defaultSafetySourceIssueId",
                )
                .setSeverityLevel(severityLevel)
                .setGroupId(groupId)
                .build()

        fun encodeIssueId(
            sourceIssueId: String,
            sourceId: String = "defaultSource",
            issueTypeId: String = "defaultIssueTypeId",
        ): String =
            SafetyCenterIds.encodeToString(
                SafetyCenterIssueId.newBuilder()
                    .setSafetyCenterIssueKey(
                        SafetyCenterIssueKey.newBuilder()
                            .setSafetySourceId(sourceId)
                            .setSafetySourceIssueId(sourceIssueId)
                            .setUserId(UserHandle.myUserId())
                            .build()
                    )
                    .setIssueTypeId(issueTypeId)
                    .build()
            )
    }
}
