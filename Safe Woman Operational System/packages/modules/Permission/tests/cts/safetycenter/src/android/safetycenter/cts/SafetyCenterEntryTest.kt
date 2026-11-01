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
import android.os.Build.VERSION_CODES
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.os.UserHandle
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsDisabled
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.safetycenter.SafetyCenterEntry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.os.ParcelableSubject.assertThat
import androidx.test.filters.SdkSuppress
import com.android.compatibility.common.util.ApiTest
import com.android.safetycenter.testing.EqualsHashCodeToStringTester
import com.android.safetycenter.testing.SafetyCenterTestHelper.Companion.createSafetyCenterEntryBuilder
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CTS tests for [SafetyCenterEntry]. */
@RunWith(AndroidJUnit4::class)
class SafetyCenterEntryTest {

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

    private val iconAction1 =
        SafetyCenterEntry.IconAction(
            SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
            pendingIntent1,
        )
    private val iconAction2 =
        SafetyCenterEntry.IconAction(
            SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
            pendingIntent2,
        )

    private val entry1 =
        createSafetyCenterEntryBuilder("eNtRy_iD", "a title", UserHandle.of(1), "source_id")
            .setSummary("a summary")
            .setPendingIntent(pendingIntent1)
            .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_UNKNOWN)
            .setIconAction(iconAction1)
            .build()

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getId"])
    fun getId_returnsId() {
        assertThat(SafetyCenterEntry.Builder(entry1).setId("id_one").build().id).isEqualTo("id_one")
        assertThat(SafetyCenterEntry.Builder(entry1).setId("id_two").build().id).isEqualTo("id_two")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getTitle"])
    fun getTitle_returnsTitle() {
        assertThat(SafetyCenterEntry.Builder(entry1).setTitle("a title").build().title)
            .isEqualTo("a title")
        assertThat(SafetyCenterEntry.Builder(entry1).setTitle("another title").build().title)
            .isEqualTo("another title")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getSummary"])
    fun getSummary_returnsSummary() {
        assertThat(SafetyCenterEntry.Builder(entry1).setSummary("a summary").build().summary)
            .isEqualTo("a summary")
        assertThat(SafetyCenterEntry.Builder(entry1).setSummary("another summary").build().summary)
            .isEqualTo("another summary")
        assertThat(SafetyCenterEntry.Builder(entry1).setSummary(null).build().summary).isNull()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getSeverityLevel"])
    fun getSeverityLevel_returnsSeverityLevel() {
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_RECOMMENDATION)
                    .build()
                    .severityLevel
            )
            .isEqualTo(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_RECOMMENDATION)
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
                    .build()
                    .severityLevel
            )
            .isEqualTo(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getSeverityUnspecifiedIconType"])
    fun getSeverityUnspecifiedIconType_returnsSeverityUnspecifiedIconType() {
        assertThat(entry1.severityUnspecifiedIconType)
            .isEqualTo(SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_NO_ICON)
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .build()
                    .severityUnspecifiedIconType
            )
            .isEqualTo(SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#isEnabled"])
    fun isEnabled_returnsIsEnabled() {
        assertThat(SafetyCenterEntry.Builder(entry1).setEnabled(true).build().isEnabled).isTrue()
        assertThat(SafetyCenterEntry.Builder(entry1).setEnabled(false).build().isEnabled).isFalse()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#isEnabled"])
    fun isEnabled_defaultTrue() {
        assertThat(
                createSafetyCenterEntryBuilder("eNtRy_iD", "a title", UserHandle.of(1), "source_id")
                    .setPendingIntent(pendingIntent1)
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_UNKNOWN)
                    .build()
                    .isEnabled
            )
            .isTrue()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getPendingIntent"])
    fun getPendingIntent_returnsPendingIntent() {
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setPendingIntent(pendingIntent1)
                    .build()
                    .pendingIntent
            )
            .isEqualTo(pendingIntent1)
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setPendingIntent(pendingIntent2)
                    .build()
                    .pendingIntent
            )
            .isEqualTo(pendingIntent2)
        assertThat(SafetyCenterEntry.Builder(entry1).setPendingIntent(null).build().pendingIntent)
            .isNull()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getIconAction"])
    fun getIconAction_returnsIconAction() {
        assertThat(SafetyCenterEntry.Builder(entry1).setIconAction(iconAction1).build().iconAction)
            .isEqualTo(iconAction1)
        assertThat(SafetyCenterEntry.Builder(entry1).setIconAction(iconAction2).build().iconAction)
            .isEqualTo(iconAction2)
        assertThat(SafetyCenterEntry.Builder(entry1).setIconAction(null).build().iconAction)
            .isNull()
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#hasError"])
    fun hasError_returnsHasError() {
        assertThat(entry1.hasError()).isFalse()
        assertThat(SafetyCenterEntry.Builder(entry1).setHasError(true).build().hasError()).isTrue()
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getUser"])
    fun getUserHandle_returnsUser() {
        assertThat(entry1.user).isEqualTo(UserHandle.of(1))
        assertThat(SafetyCenterEntry.Builder(entry1).setUser(UserHandle.of(123)).build().user)
            .isEqualTo(UserHandle.of(123))
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#getSafetySourceId"])
    fun getSafetySourceId_returnsSafetySourceId() {
        assertThat(entry1.safetySourceId).isEqualTo("source_id")
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setSafetySourceId("custom_source_id")
                    .build()
                    .safetySourceId
            )
            .isEqualTo("custom_source_id")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry.Builder#setSeverityLevel"])
    fun build_withInvalidEntrySeverityLevel_throwsIllegalArgumentException() {
        val exception =
            assertFailsWith(IllegalArgumentException::class) {
                SafetyCenterEntry.Builder(entry1).setSeverityLevel(-1)
            }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Unexpected EntrySeverityLevel for SafetyCenterEntry: -1")
    }

    @Test
    @ApiTest(
        apis = ["android.safetycenter.SafetyCenterEntry.Builder#setSeverityUnspecifiedIconType"]
    )
    fun build_withInvalidSeverityUnspecifiedIconType_throwsIllegalArgumentException() {
        val exception =
            assertFailsWith(IllegalArgumentException::class) {
                SafetyCenterEntry.Builder(entry1).setSeverityUnspecifiedIconType(-1)
            }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Unexpected SeverityUnspecifiedIconType for SafetyCenterEntry: -1")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry#describeContents"])
    fun describeContents_returns0() {
        assertThat(entry1.describeContents()).isEqualTo(0)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.CREATOR",
                "android.safetycenter.SafetyCenterEntry#writeToParcel",
            ]
    )
    fun parcelRoundTrip_recreatesEqual() {
        assertThat(entry1).recreatesEqual(SafetyCenterEntry.CREATOR)
        assertThat(
                SafetyCenterEntry.Builder(entry1)
                    .setSummary(null)
                    .setPendingIntent(null)
                    .setIconAction(null)
                    .build()
            )
            .recreatesEqual(SafetyCenterEntry.CREATOR)
    }

    @Test
    @RequiresFlagsDisabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @SdkSuppress(minSdkVersion = BAKLAVA)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.CREATOR",
                "android.safetycenter.SafetyCenterEntry#writeToParcel",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_onBPlus_recreatesEqual() {
        assertThat(SafetyCenterEntry.Builder("id", "title").build())
            .recreatesEqual(SafetyCenterEntry.CREATOR)
    }

    @Test
    @SdkSuppress(maxSdkVersion = VANILLA_ICE_CREAM)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.CREATOR",
                "android.safetycenter.SafetyCenterEntry#writeToParcel",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_recreatesEqual() {
        assertThat(SafetyCenterEntry.Builder("id", "title").build())
            .recreatesEqual(SafetyCenterEntry.CREATOR)
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.CREATOR",
                "android.safetycenter.SafetyCenterEntry#writeToParcel",
            ]
    )
    fun parcelRoundTrip_whenOpenSafetyCenterApisEnabled_recreatesEqual() {
        assertThat(SafetyCenterEntry.Builder(entry1).setHasError(true).build())
            .recreatesEqual(SafetyCenterEntry.CREATOR)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry#equals",
                "android.safetycenter.SafetyCenterEntry#hashCode",
                "android.safetycenter.SafetyCenterEntry#toString",
            ]
    )
    fun equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterEntry.CREATOR,
                createCopy = { SafetyCenterEntry.Builder(it).build() },
            )
            .addEqualityGroup(entry1)
            .addEqualityGroup(
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(1), "source_id")
                    .setSummary("a summary")
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .setPendingIntent(pendingIntent1)
                    .setIconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                        pendingIntent2,
                    )
                    .build(),
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(1), "source_id")
                    .setSummary("a summary")
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .setPendingIntent(pendingIntent1)
                    .setIconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                        pendingIntent2,
                    )
                    .build(),
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setId("a different id").build())
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setTitle("a different title").build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setSummary("a different summary").build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_CRITICAL_WARNING)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .build()
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setEnabled(false).build())
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setPendingIntent(pendingIntent2).build()
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setIconAction(iconAction2).build())
            .test()
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry#equals",
                "android.safetycenter.SafetyCenterEntry#hashCode",
                "android.safetycenter.SafetyCenterEntry#toString",
            ]
    )
    fun equalsHashCodeToString_whenOpenSafetyCenterApisEnabled_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterEntry.CREATOR,
                createCopy = { SafetyCenterEntry.Builder(it).build() },
            )
            .addEqualityGroup(entry1)
            .addEqualityGroup(
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(1), "source_id")
                    .setSummary("a summary")
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .setPendingIntent(pendingIntent1)
                    .setIconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                        pendingIntent2,
                    )
                    .build(),
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(1), "source_id")
                    .setSummary("a summary")
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_OK)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .setPendingIntent(pendingIntent1)
                    .setIconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                        pendingIntent2,
                    )
                    .build(),
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setId("a different id").build())
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setTitle("a different title").build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setSummary("a different summary").build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityLevel(SafetyCenterEntry.ENTRY_SEVERITY_LEVEL_CRITICAL_WARNING)
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1)
                    .setSeverityUnspecifiedIconType(
                        SafetyCenterEntry.SEVERITY_UNSPECIFIED_ICON_TYPE_PRIVACY
                    )
                    .build()
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setEnabled(false).build())
            .addEqualityGroup(
                SafetyCenterEntry.Builder(entry1).setPendingIntent(pendingIntent2).build()
            )
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setIconAction(iconAction2).build())
            .addEqualityGroup(SafetyCenterEntry.Builder(entry1).setHasError(true).build())
            .addEqualityGroup(
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(123), "source_id")
                    .build()
            )
            .addEqualityGroup(
                createSafetyCenterEntryBuilder("id", "a title", UserHandle.of(123), "source_id")
                    .setHasError(true)
                    .build()
            )
            .addEqualityGroup(
                createSafetyCenterEntryBuilder(
                        "id",
                        "a title",
                        UserHandle.of(123),
                        "custom_safety_source_id",
                    )
                    .build()
            )
            .test()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry.IconAction#getType"])
    fun iconAction_getType_returnsType() {
        assertThat(
                SafetyCenterEntry.IconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
                        pendingIntent1,
                    )
                    .type
            )
            .isEqualTo(SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR)
        assertThat(
                SafetyCenterEntry.IconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                        pendingIntent1,
                    )
                    .type
            )
            .isEqualTo(SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry.IconAction#getPendingIntent"])
    fun iconAction_getPendingIntent_returnsPendingIntent() {
        assertThat(
                SafetyCenterEntry.IconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
                        pendingIntent1,
                    )
                    .pendingIntent
            )
            .isEqualTo(pendingIntent1)
        assertThat(
                SafetyCenterEntry.IconAction(
                        SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
                        pendingIntent2,
                    )
                    .pendingIntent
            )
            .isEqualTo(pendingIntent2)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry.IconAction#<init>"])
    fun iconAction_withInvalidIconActionType_throwsIllegalArgumentException() {
        val exception =
            assertFailsWith(IllegalArgumentException::class) {
                SafetyCenterEntry.IconAction(-1, pendingIntent1)
            }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Unexpected IconActionType for IconAction: -1")
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterEntry.IconAction#describeContents"])
    fun iconAction_describeContents_returns0() {
        assertThat(iconAction1.describeContents()).isEqualTo(0)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.IconAction.CREATOR",
                "android.safetycenter.SafetyCenterEntry.IconAction#writeToParcel",
            ]
    )
    fun iconAction_parcelRoundTrip_recreatesEqual() {
        assertThat(iconAction1).recreatesEqual(SafetyCenterEntry.IconAction.CREATOR)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterEntry.IconAction#equals",
                "android.safetycenter.SafetyCenterEntry.IconAction#hashCode",
                "android.safetycenter.SafetyCenterEntry.IconAction#toString",
            ]
    )
    fun iconAction_equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterEntry.IconAction.CREATOR
            )
            .addEqualityGroup(
                iconAction1,
                SafetyCenterEntry.IconAction(
                    SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
                    pendingIntent1,
                ),
            )
            .addEqualityGroup(
                iconAction2,
                SafetyCenterEntry.IconAction(
                    SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                    pendingIntent2,
                ),
            )
            .addEqualityGroup(
                SafetyCenterEntry.IconAction(
                    SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_INFO,
                    pendingIntent1,
                )
            )
            .addEqualityGroup(
                SafetyCenterEntry.IconAction(
                    SafetyCenterEntry.IconAction.ICON_ACTION_TYPE_GEAR,
                    pendingIntent2,
                )
            )
            .test()
    }
}
