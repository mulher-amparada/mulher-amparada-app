/*
 * Copyright (C) 2022 The Android Open Source Project
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

package android.safetycenter.cts

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.os.UserHandle
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsDisabled
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.safetycenter.SafetyCenterIssue
import android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.os.ParcelableSubject.assertThat
import androidx.test.filters.SdkSuppress
import com.android.compatibility.common.util.ApiTest
import com.android.safetycenter.testing.EqualsHashCodeToStringTester
import com.android.safetycenter.testing.SafetyCenterTestHelper.Companion.createSafetyCenterIssueBuilder
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CTS tests for [SafetyCenterIssue]. */
@RunWith(AndroidJUnit4::class)
class SafetyCenterIssueTest {

    @get:Rule val flagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val pendingIntent1 =
        PendingIntent.getActivity(context, 0, Intent("Fake Data"), PendingIntent.FLAG_IMMUTABLE)
    private val pendingIntent2 =
        PendingIntent.getActivity(
            context,
            0,
            Intent("Fake Different Data"),
            PendingIntent.FLAG_IMMUTABLE,
        )

    private val action1 =
        SafetyCenterIssue.Action.Builder("action_id_1", "an action", pendingIntent1)
            .setWillResolve(true)
            .setIsInFlight(true)
            .setSuccessMessage("a success message")
            .build()
    private val action2 =
        SafetyCenterIssue.Action.Builder("action_id_2", "another action", pendingIntent2)
            .setWillResolve(false)
            .setIsInFlight(false)
            .build()

    private val issue1 =
        createSafetyCenterIssueBuilder(
                "issue_id",
                "Everything's good",
                "Please acknowledge this",
                UserHandle.of(1),
                setOf("safety_source_id_1"),
                "issue_type_id_1",
                "safety_source_issue_id_1",
            )
            .setSubtitle("In the neighborhood")
            .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
            .setDismissible(true)
            .setShouldConfirmDismissal(true)
            .setActions(listOf(action1))
            .build()

    private val issueWithRequiredFieldsOnly =
        createSafetyCenterIssueBuilder(
                "issue_id",
                "Everything's good",
                "Please acknowledge this",
                UserHandle.of(1),
                setOf("safety_source_id_1"),
                "issue_type_id_1",
                "safety_source_issue_id_1",
            )
            .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
            .build()

    @Test
    fun getId_returnsId() {
        assertThat(SafetyCenterIssue.Builder(issue1).setId("an id").build().id).isEqualTo("an id")
        assertThat(SafetyCenterIssue.Builder(issue1).setId("another id").build().id)
            .isEqualTo("another id")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getTitle"])
    fun getTitle_returnsTitle() {
        assertThat(SafetyCenterIssue.Builder(issue1).setTitle("a title").build().title)
            .isEqualTo("a title")
        assertThat(SafetyCenterIssue.Builder(issue1).setTitle("another title").build().title)
            .isEqualTo("another title")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getSubtitle"])
    fun getSubtitle_returnsSubtitle() {
        assertThat(SafetyCenterIssue.Builder(issue1).setSubtitle("a subtitle").build().subtitle)
            .isEqualTo("a subtitle")
        assertThat(
                SafetyCenterIssue.Builder(issue1).setSubtitle("another subtitle").build().subtitle
            )
            .isEqualTo("another subtitle")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getSummary"])
    fun getSummary_returnsSummary() {
        assertThat(SafetyCenterIssue.Builder(issue1).setSummary("a summary").build().summary)
            .isEqualTo("a summary")
        assertThat(SafetyCenterIssue.Builder(issue1).setSummary("another summary").build().summary)
            .isEqualTo("another summary")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getAttributionTitle"])
    fun getAttributionTitle_returnsAttributionTitle() {
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setAttributionTitle("an attributionTitle")
                    .build()
                    .attributionTitle
            )
            .isEqualTo("an attributionTitle")
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setAttributionTitle("another attributionTitle")
                    .build()
                    .attributionTitle
            )
            .isEqualTo("another attributionTitle")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getAttributionTitle"])
    fun getAttributionTitle_withNullAttributionTitle_returnsNull() {
        val safetyCenterIssue =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .build()

        assertThat(safetyCenterIssue.attributionTitle).isNull()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getSeverityLevel"])
    fun getSeverityLevel_returnsSeverityLevel() {
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_RECOMMENDATION)
                    .build()
                    .severityLevel
            )
            .isEqualTo(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_RECOMMENDATION)
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING)
                    .build()
                    .severityLevel
            )
            .isEqualTo(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#isDismissible"])
    fun isDismissible_returnsIsDismissible() {
        assertThat(SafetyCenterIssue.Builder(issue1).setDismissible(true).build().isDismissible)
            .isTrue()
        assertThat(SafetyCenterIssue.Builder(issue1).setDismissible(false).build().isDismissible)
            .isFalse()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#isDismissible"])
    fun isDismissible_defaultsToTrue() {
        assertThat(issueWithRequiredFieldsOnly.isDismissible).isTrue()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#shouldConfirmDismissal"])
    fun shouldConfirmDismissal_returnsShouldConfirmDismissal() {
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setShouldConfirmDismissal(true)
                    .build()
                    .shouldConfirmDismissal()
            )
            .isTrue()
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setShouldConfirmDismissal(false)
                    .build()
                    .shouldConfirmDismissal()
            )
            .isFalse()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#shouldConfirmDismissal"])
    fun shouldConfirmDismissal_defaultsToTrue() {
        assertThat(issueWithRequiredFieldsOnly.shouldConfirmDismissal()).isTrue()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getActions"])
    fun getActions_returnsActions() {
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setActions(listOf(action1, action2))
                    .build()
                    .actions
            )
            .containsExactly(action1, action2)
            .inOrder()
        assertThat(SafetyCenterIssue.Builder(issue1).setActions(listOf(action2)).build().actions)
            .containsExactly(action2)
        assertThat(SafetyCenterIssue.Builder(issue1).setActions(listOf()).build().actions).isEmpty()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getActions"])
    fun getActions_mutationsAreNotAllowed() {
        val mutatedActions = issue1.actions

        assertFailsWith(UnsupportedOperationException::class) { mutatedActions.add(action2) }
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Builder#setSeverityLevel"])
    fun build_withInvalidIssueSeverityLevel_throwsIllegalArgumentException() {
        val exception =
            assertFailsWith(IllegalArgumentException::class) {
                SafetyCenterIssue.Builder(issue1).setSeverityLevel(-1)
            }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Unexpected IssueSeverityLevel for SafetyCenterIssue: -1")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getGroupId"])
    fun getGroupId_withNonNullValue_returnsGroupId() {
        val issue = SafetyCenterIssue.Builder(issue1).setGroupId("group_id").build()

        assertThat(issue.groupId).isEqualTo("group_id")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getGroupId"])
    fun getGroupId_withNullValue_returnsNull() {
        val issue =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .build()

        assertThat(issue.groupId).isNull()
    }

    @Test
    @SdkSuppress(maxSdkVersion = TIRAMISU)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getGroupId"])
    fun getGroupId_withVersionLessThanU_throws() {
        val issue =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .build()

        assertFails { issue.groupId }
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Builder#setGroupId"])
    fun setGroupId_withNullValue_returnsNull() {
        val issue = SafetyCenterIssue.Builder(issue1).setGroupId(null).build()

        assertThat(issue.groupId).isNull()
    }

    @Test
    @SdkSuppress(maxSdkVersion = TIRAMISU)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Builder#setGroupId"])
    fun setGroupId_withVersionLessThanU_throws() {
        assertFails { SafetyCenterIssue.Builder(issue1).setGroupId("group_id").build() }
    }

    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getUser"])
    fun getUser_returnsUser() {
        assertThat(issue1.user).isEqualTo(UserHandle.of(1))
        assertThat(SafetyCenterIssue.Builder(issue1).setUser(UserHandle.of(123)).build().user)
            .isEqualTo(UserHandle.of(123))
    }

    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getSafetySourceIds"])
    fun getSafetySourceIds_returnsSafetySourceIds() {
        assertThat(issue1.safetySourceIds).containsExactly("safety_source_id_1")
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setSafetySourceIds(setOf("safety_source_id_1", "safety_source_id_2"))
                    .build()
                    .safetySourceIds
            )
            .containsExactly("safety_source_id_1", "safety_source_id_2")
    }

    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getIssueTypeId"])
    fun getIssueTypeId_returnsIssueTypeId() {
        assertThat(issue1.issueTypeId).isEqualTo("issue_type_id_1")
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setIssueTypeId("issue_type_id_2")
                    .build()
                    .issueTypeId
            )
            .isEqualTo("issue_type_id_2")
    }

    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#getSafetySourceIssueId"])
    fun getSafetySourceIssueId_returnSafetySourceIssueId() {
        assertThat(issue1.safetySourceIssueId).isEqualTo("safety_source_issue_id_1")
        assertThat(
                SafetyCenterIssue.Builder(issue1)
                    .setSafetySourceIssueId("safety_source_issue_id_2")
                    .build()
                    .safetySourceIssueId
            )
            .isEqualTo("safety_source_issue_id_2")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue#describeContents"])
    fun describeContents_returns0() {
        assertThat(issue1.describeContents()).isEqualTo(0)
        assertThat(issueWithRequiredFieldsOnly.describeContents()).isEqualTo(0)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.CREATOR",
                "android.safetycenter.SafetyCenterIssue#writeToParcel",
            ]
    )
    fun parcelRoundTrip_recreatesEqual() {
        assertThat(issue1).recreatesEqual(SafetyCenterIssue.CREATOR)
        assertThat(issueWithRequiredFieldsOnly).recreatesEqual(SafetyCenterIssue.CREATOR)
    }

    @Test
    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsDisabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.CREATOR",
                "android.safetycenter.SafetyCenterIssue#writeToParcel",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_onBPlus_recreatesEqual() {
        assertThat(SafetyCenterIssue.Builder("id", "title", "summary").build())
            .recreatesEqual(SafetyCenterIssue.CREATOR)
    }

    @Test
    @SdkSuppress(maxSdkVersion = VANILLA_ICE_CREAM)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.CREATOR",
                "android.safetycenter.SafetyCenterIssue#writeToParcel",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_recreatesEqual() {
        assertThat(SafetyCenterIssue.Builder("id", "title", "summary").build())
            .recreatesEqual(SafetyCenterIssue.CREATOR)
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.CREATOR",
                "android.safetycenter.SafetyCenterIssue#writeToParcel",
            ]
    )
    fun parcelRoundTrip_recreatesEqual_atLeastAndroidU() {
        val safetyCenterIssue =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .setSubtitle("In the neighborhood")
                .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
                .setDismissible(true)
                .setShouldConfirmDismissal(true)
                .setActions(
                    listOf(
                        SafetyCenterIssue.Action.Builder(action1)
                            .setConfirmationDialogDetails(
                                ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
                            )
                            .build()
                    )
                )
                .setAttributionTitle("Attribution title")
                .setGroupId("group_id")
                .build()

        assertThat(safetyCenterIssue).recreatesEqual(SafetyCenterIssue.CREATOR)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue#equals",
                "android.safetycenter.SafetyCenterIssue#hashCode",
                "android.safetycenter.SafetyCenterIssue#toString",
            ]
    )
    fun equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        newTiramisuEqualsHashCodeToStringTester().test()
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue#equals",
                "android.safetycenter.SafetyCenterIssue#hashCode",
                "android.safetycenter.SafetyCenterIssue#toString",
            ]
    )
    fun equalsHashCodeToString_usingEqualsHashCodeToStringTester_atLeastAndroidU() {
        newUpsideDownCakeEqualsHashCodeToStringTester().test()
    }

    @Test
    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue#equals",
                "android.safetycenter.SafetyCenterIssue#hashCode",
                "android.safetycenter.SafetyCenterIssue#toString",
            ]
    )
    fun equalsHashCodeToString_usingEqualsHashCodeToStringTester_withOpenScApisFlagOn() {
        newBaklavaEqualsHashCodeToStringTester().test()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getId"])
    fun action_getId_returnsId() {
        assertThat(action1.id).isEqualTo("action_id_1")
        assertThat(action2.id).isEqualTo("action_id_2")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getLabel"])
    fun action_getLabel_returnsLabel() {
        assertThat(action1.label).isEqualTo("an action")
        assertThat(action2.label).isEqualTo("another action")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getPendingIntent"])
    fun action_getPendingIntent_returnsPendingIntent() {
        assertThat(action1.pendingIntent).isEqualTo(pendingIntent1)
        assertThat(action2.pendingIntent).isEqualTo(pendingIntent2)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#willResolve"])
    fun action_willResolve_returnsWillResolve() {
        assertThat(action1.willResolve()).isTrue()
        assertThat(action2.willResolve()).isFalse()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#isInFlight"])
    fun action_isInFlight_returnsIsInFlight() {
        assertThat(action1.isInFlight).isTrue()
        assertThat(action2.isInFlight).isFalse()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getSuccessMessage"])
    fun action_getSuccessMessage_returnsSuccessMessage() {
        assertThat(action1.successMessage).isEqualTo("a success message")
        assertThat(action2.successMessage).isNull()
    }

    @Test
    @SdkSuppress(maxSdkVersion = TIRAMISU)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getConfirmationDialogDetails"])
    fun action_getConfirmationDialogDetails_withVersionLessThanU_throws() {
        assertFails { action1.confirmationDialogDetails }
    }

    @Test
    @SdkSuppress(maxSdkVersion = TIRAMISU)
    @ApiTest(
        apis =
            ["android.safetycenter.SafetyCenterIssue.Action.Builder#setConfirmationDialogDetails"]
    )
    fun action_setConfirmationDialogDetails_withVersionLessThanU_throws() {
        assertFails {
            SafetyCenterIssue.Action.Builder("action_id", "Action label", pendingIntent1)
                .setConfirmationDialogDetails(
                    ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
                )
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getConfirmationDialogDetails"])
    fun action_getConfirmationDialogDetails_withDefaultBuilder_returnsNull() {
        val action =
            SafetyCenterIssue.Action.Builder("action_id", "Action label", pendingIntent1).build()

        assertThat(action.confirmationDialogDetails).isNull()
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#getConfirmationDialogDetails"])
    fun action_getConfirmationDialogDetails_whenSetExplicitly_returnsConfirmation() {
        val action =
            SafetyCenterIssue.Action.Builder("action_id", "Action label", pendingIntent1)
                .setConfirmationDialogDetails(
                    ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
                )
                .build()

        assertThat(action.confirmationDialogDetails)
            .isEqualTo(ConfirmationDialogDetails("Title", "Text", "Accept", "Deny"))
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterIssue.Action#describeContents"])
    fun action_describeContents_returns0() {
        assertThat(action1.describeContents()).isEqualTo(0)
        assertThat(action2.describeContents()).isEqualTo(0)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.CREATOR",
                "android.safetycenter.SafetyCenterIssue.Action#writeToParcel",
            ]
    )
    fun action_parcelRoundTrip_recreatesEqual() {
        assertThat(action1).recreatesEqual(SafetyCenterIssue.Action.CREATOR)
        assertThat(action2).recreatesEqual(SafetyCenterIssue.Action.CREATOR)
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.CREATOR",
                "android.safetycenter.SafetyCenterIssue.Action#writeToParcel",
            ]
    )
    fun action_parcelRoundTrip_recreatesEqual_atLeastAndroidU() {
        val action =
            SafetyCenterIssue.Action.Builder(action1)
                .setConfirmationDialogDetails(
                    ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
                )
                .build()

        assertThat(action).recreatesEqual(SafetyCenterIssue.Action.CREATOR)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action#equals",
                "android.safetycenter.SafetyCenterIssue.Action#hashCode",
                "android.safetycenter.SafetyCenterIssue.Action#toString",
            ]
    )
    fun action_equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        issueActionNewTiramisuEqualsHashCodeToStringTester().test()
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action#equals",
                "android.safetycenter.SafetyCenterIssue.Action#hashCode",
                "android.safetycenter.SafetyCenterIssue.Action#toString",
            ]
    )
    fun action_equalsHashCodeToString_usingEqualsHashCodeToStringTester_atLeastAndroidU() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
        issueActionNewTiramisuEqualsHashCodeToStringTester(
                createCopyFromBuilder = { SafetyCenterIssue.Action.Builder(it).build() }
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder(action1)
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder(action2)
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build(),
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("a_different_id", "a label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a different label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent2)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setSuccessMessage("a different success message")
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setId("another_id")
                    .setLabel("another_label")
                    .setPendingIntent(pendingIntent2)
                    .setConfirmationDialogDetails(confirmationDialogDetails)
                    .build()
            )
            .test()
    }

    /**
     * Creates a new [EqualsHashCodeToStringTester] instance with all the equality groups in the
     * [newTiramisuEqualsHashCodeToStringTester] plus new equality groups covering all the new
     * fields added in U.
     */
    @RequiresApi(UPSIDE_DOWN_CAKE)
    private fun newUpsideDownCakeEqualsHashCodeToStringTester():
        EqualsHashCodeToStringTester<SafetyCenterIssue> {
        val issueWithTiramisuFields =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .setSubtitle("In the neighborhood")
                .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
                .setDismissible(true)
                .setShouldConfirmDismissal(true)
                .setActions(listOf(action1))
                .build()
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
        return newTiramisuEqualsHashCodeToStringTester(
                createCopyFromBuilder = { SafetyCenterIssue.Builder(it).build() }
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setAttributionTitle("Attribution title")
                    .build(),
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setAttributionTitle("Attribution title")
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setAttributionTitle("a different attribution title")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setAttributionTitle("Attribution title")
                    .setGroupId("group_id")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields).setGroupId("group_id").build(),
                SafetyCenterIssue.Builder(issueWithTiramisuFields).setGroupId("group_id").build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setGroupId("a different group_id")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithTiramisuFields)
                    .setActions(
                        listOf(
                            SafetyCenterIssue.Action.Builder(action1)
                                .setConfirmationDialogDetails(confirmationDialogDetails)
                                .build()
                        )
                    )
                    .build()
            )
    }

    /**
     * Creates a new [EqualsHashCodeToStringTester] instance with all the equality groups in the
     * [newUpsideDownCakeEqualsHashCodeToStringTester] plus new equality groups covering all the new
     * fields added in Baklava.
     */
    @RequiresApi(BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    private fun newBaklavaEqualsHashCodeToStringTester():
        EqualsHashCodeToStringTester<SafetyCenterIssue> {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")
        val issueWithUpsideDownCakeFields =
            createSafetyCenterIssueBuilder(
                    "issue_id",
                    "Everything's good",
                    "Please acknowledge this",
                    UserHandle.of(1),
                    setOf("safety_source_id_1"),
                    "issue_type_id_1",
                    "safety_source_issue_id_1",
                )
                .setSubtitle("In the neighborhood")
                .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
                .setDismissible(true)
                .setShouldConfirmDismissal(true)
                .setActions(
                    listOf(
                        SafetyCenterIssue.Action.Builder(action1)
                            .setConfirmationDialogDetails(confirmationDialogDetails)
                            .build()
                    )
                )
                .setAttributionTitle("Attribution title")
                .setGroupId("group_id")
                .build()
        return newUpsideDownCakeEqualsHashCodeToStringTester()
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setUser(UserHandle.of(123))
                    .build(),
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setUser(UserHandle.of(123))
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setSafetySourceIds(setOf("safety_source_id_5", "safety_source_id_1"))
                    .build(),
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setSafetySourceIds(setOf("safety_source_id_1", "safety_source_id_5"))
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setIssueTypeId("issue_type_id_2")
                    .build(),
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setIssueTypeId("issue_type_id_2")
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setSafetySourceIssueId("safety_source_issue_id_2")
                    .build(),
                SafetyCenterIssue.Builder(issueWithUpsideDownCakeFields)
                    .setSafetySourceIssueId("safety_source_issue_id_2")
                    .build(),
            )
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis = ["android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#getTitle"]
    )
    fun actionConfirmation_getTitle_returnsTitle() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails.title).isEqualTo("Title")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis = ["android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#getText"]
    )
    fun actionConfirmation_getText_returnsText() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails.text).isEqualTo("Text")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#getAcceptButtonText"
            ]
    )
    fun actionConfirmation_getAcceptButtonText_returnsAcceptButtonText() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails.acceptButtonText).isEqualTo("Accept")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#getDenyButtonText"
            ]
    )
    fun actionConfirmation_getDenyButtonText_returnsDenyButtonText() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails.denyButtonText).isEqualTo("Deny")
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#describeContents"
            ]
    )
    fun actionConfirmation_describeContents_returns0() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails.describeContents()).isEqualTo(0)
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails.CREATOR",
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#writeToParcel",
            ]
    )
    fun actionConfirmation_parcelRoundTrip_recreatesEqual() {
        val confirmationDialogDetails = ConfirmationDialogDetails("Title", "Text", "Accept", "Deny")

        assertThat(confirmationDialogDetails).recreatesEqual(ConfirmationDialogDetails.CREATOR)
    }

    @Test
    @SdkSuppress(minSdkVersion = UPSIDE_DOWN_CAKE)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#equals",
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#hashCode",
                "android.safetycenter.SafetyCenterIssue.Action.ConfirmationDialogDetails#toString",
            ]
    )
    fun actionConfirmation_equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = ConfirmationDialogDetails.CREATOR
            )
            .addEqualityGroup(
                ConfirmationDialogDetails("Title", "Text", "Accept", "Deny"),
                ConfirmationDialogDetails("Title", "Text", "Accept", "Deny"),
            )
            .addEqualityGroup(ConfirmationDialogDetails("Other title", "Text", "Accept", "Deny"))
            .addEqualityGroup(ConfirmationDialogDetails("Title", "Other text", "Accept", "Deny"))
            .addEqualityGroup(ConfirmationDialogDetails("Title", "Text", "Other accept", "Deny"))
            .addEqualityGroup(ConfirmationDialogDetails("Title", "Text", "Accept", "Other deny"))
            .test()
    }

    /**
     * Creates a new [EqualsHashCodeToStringTester] instance which covers all the fields in the T
     * API and is safe to use on any T+ API level.
     */
    private fun newTiramisuEqualsHashCodeToStringTester(
        createCopyFromBuilder: ((SafetyCenterIssue) -> SafetyCenterIssue)? = null
    ) =
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterIssue.CREATOR,
                createCopy = createCopyFromBuilder,
            )
            .addEqualityGroup(issue1, SafetyCenterIssue.Builder(issue1).build())
            .addEqualityGroup(issueWithRequiredFieldsOnly)
            .addEqualityGroup(
                createSafetyCenterIssueBuilder(
                        "an id",
                        "a title",
                        "Please acknowledge this",
                        UserHandle.of(1),
                        setOf("safety_source_id_1"),
                        "issue_type_id_1",
                        "safety_source_issue_id_1",
                    )
                    .setSubtitle("In the neighborhood")
                    .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
                    .setActions(listOf(action1))
                    .build(),
                createSafetyCenterIssueBuilder(
                        "an id",
                        "a title",
                        "Please acknowledge this",
                        UserHandle.of(1),
                        setOf("safety_source_id_1"),
                        "issue_type_id_1",
                        "safety_source_issue_id_1",
                    )
                    .setSubtitle("In the neighborhood")
                    .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK)
                    .setActions(listOf(action1))
                    .build(),
            )
            .addEqualityGroup(SafetyCenterIssue.Builder(issue1).setId("a different id").build())
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issue1).setTitle("a different title").build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issue1).setSubtitle("a different subtitle").build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issue1).setSummary("a different summary").build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issue1)
                    .setSeverityLevel(SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING)
                    .build()
            )
            .addEqualityGroup(SafetyCenterIssue.Builder(issue1).setDismissible(false).build())
            .addEqualityGroup(
                SafetyCenterIssue.Builder(issue1).setShouldConfirmDismissal(false).build()
            )
            .addEqualityGroup(SafetyCenterIssue.Builder(issue1).setActions(listOf(action2)).build())

    private fun issueActionNewTiramisuEqualsHashCodeToStringTester(
        createCopyFromBuilder: ((SafetyCenterIssue.Action) -> SafetyCenterIssue.Action)? = null
    ) =
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterIssue.Action.CREATOR,
                createCopy = createCopyFromBuilder,
            )
            .addEqualityGroup(action1)
            .addEqualityGroup(action2)
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .build(),
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("a_different_id", "a label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a different label", pendingIntent1)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent2)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setWillResolve(true)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setIsInFlight(true)
                    .setSuccessMessage("a success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setSuccessMessage("a different success message")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterIssue.Action.Builder("an_id", "a label", pendingIntent1)
                    .setId("another_id")
                    .setLabel("another_label")
                    .setPendingIntent(pendingIntent2)
                    .build()
            )
}
