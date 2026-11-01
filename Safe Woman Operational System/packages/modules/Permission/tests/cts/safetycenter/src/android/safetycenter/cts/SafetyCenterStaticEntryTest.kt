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
import android.safetycenter.SafetyCenterStaticEntry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.os.ParcelableSubject.assertThat
import androidx.test.filters.SdkSuppress
import com.android.compatibility.common.util.ApiTest
import com.android.safetycenter.testing.EqualsHashCodeToStringTester
import com.android.safetycenter.testing.SafetyCenterTestHelper.Companion.createSafetyCenterStaticEntryBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CTS tests for [SafetyCenterStaticEntry]. */
@RunWith(AndroidJUnit4::class)
class SafetyCenterStaticEntryTest {

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

    private val title1 = "a title"
    private val title2 = "another title"

    private val summary1 = "a summary"
    private val summary2 = "another summary"

    private val userId1 = UserHandle.of(0)
    private val userId2 = UserHandle.of(10)

    private val sourceId1 = "sourceId1"
    private val sourceId2 = "sourceId2"

    private val staticEntry1 =
        createSafetyCenterStaticEntryBuilder(title1, sourceId1, userId1)
            .setSummary(summary1)
            .setPendingIntent(pendingIntent1)
            .build()
    private val staticEntry2 =
        createSafetyCenterStaticEntryBuilder(title2, sourceId2, userId2)
            .setSummary(summary2)
            .setPendingIntent(pendingIntent2)
            .build()
    private val staticEntryMinimal =
        createSafetyCenterStaticEntryBuilder("", sourceId1, userId1).build()

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#getTitle"])
    fun getTitle_returnsTitle() {
        assertThat(staticEntry1.title).isEqualTo(title1)
        assertThat(staticEntry2.title).isEqualTo(title2)
        assertThat(SafetyCenterStaticEntry.Builder(staticEntry1).setTitle(title2).build().title)
            .isEqualTo(title2)
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#getSummary"])
    fun getSummary_returnsSummary() {
        assertThat(staticEntry1.summary).isEqualTo(summary1)
        assertThat(staticEntry2.summary).isEqualTo(summary2)
        assertThat(staticEntryMinimal.summary).isNull()
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#getPendingIntent"])
    fun getPendingIntent_returnsPendingIntent() {
        assertThat(staticEntry1.pendingIntent).isEqualTo(pendingIntent1)
        assertThat(staticEntry2.pendingIntent).isEqualTo(pendingIntent2)
        assertThat(staticEntryMinimal.pendingIntent).isNull()
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#getSafetySourceId"])
    fun getSafetySourceId_returnsSafetySourceId() {
        assertThat(staticEntry1.safetySourceId).isEqualTo(sourceId1)
        assertThat(staticEntry2.safetySourceId).isEqualTo(sourceId2)
        assertThat(
                SafetyCenterStaticEntry.Builder(staticEntry1)
                    .setSafetySourceId("custom_source")
                    .build()
                    .safetySourceId
            )
            .isEqualTo("custom_source")
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#getUser"])
    @Test
    fun getUser_returnsUser() {
        assertThat(staticEntry1.user).isEqualTo(userId1)
        assertThat(staticEntry2.user).isEqualTo(userId2)
        assertThat(
                SafetyCenterStaticEntry.Builder(staticEntry1)
                    .setUser(UserHandle.of(99))
                    .build()
                    .user
            )
            .isEqualTo(UserHandle.of(99))
    }

    @Test
    @ApiTest(apis = ["android.safetycenter.SafetyCenterStaticEntry#describeContents"])
    fun describeContents_returns0() {
        assertThat(staticEntry1.describeContents()).isEqualTo(0)
        assertThat(staticEntry2.describeContents()).isEqualTo(0)
        assertThat(staticEntryMinimal.describeContents()).isEqualTo(0)
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterStaticEntry#writeToParcel",
                "android.safetycenter.SafetyCenterStaticEntry#CREATOR",
            ]
    )
    fun parcelRoundTrip_recreatesEqual() {
        assertThat(staticEntry1).recreatesEqual(SafetyCenterStaticEntry.CREATOR)
        assertThat(staticEntry2).recreatesEqual(SafetyCenterStaticEntry.CREATOR)
        assertThat(staticEntryMinimal).recreatesEqual(SafetyCenterStaticEntry.CREATOR)
    }

    @Test
    @SdkSuppress(minSdkVersion = BAKLAVA)
    @RequiresFlagsDisabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterStaticEntry#writeToParcel",
                "android.safetycenter.SafetyCenterStaticEntry#CREATOR",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_onBPlus_recreatesEqual() {
        assertThat(SafetyCenterStaticEntry.Builder("").build())
            .recreatesEqual(SafetyCenterStaticEntry.CREATOR)
    }

    @Test
    @SdkSuppress(maxSdkVersion = VANILLA_ICE_CREAM)
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterStaticEntry#writeToParcel",
                "android.safetycenter.SafetyCenterStaticEntry#CREATOR",
            ]
    )
    fun parcelRoundTrip_forSimpleBuilderConstructor_recreatesEqual() {
        assertThat(SafetyCenterStaticEntry.Builder("").build())
            .recreatesEqual(SafetyCenterStaticEntry.CREATOR)
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.BAKLAVA)
    @RequiresFlagsEnabled(Flags.FLAG_OPEN_SAFETY_CENTER_APIS)
    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterStaticEntry#equals",
                "android.safetycenter.SafetyCenterStaticEntry#hashCode",
                "android.safetycenter.SafetyCenterStaticEntry#toString",
            ]
    )
    fun equalsHashCodeToString_whenOpenSafetyCenterApisEnabled_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterStaticEntry.CREATOR,
                createCopy = { SafetyCenterStaticEntry.Builder(it).build() },
            )
            .addEqualityGroup(
                staticEntry1,
                createSafetyCenterStaticEntryBuilder(title1, sourceId1, userId1)
                    .setSummary(summary1)
                    .setPendingIntent(pendingIntent1)
                    .build(),
            )
            .addEqualityGroup(
                createSafetyCenterStaticEntryBuilder(title1, sourceId1, userId1)
                    .setSummary(summary1)
                    .setPendingIntent(pendingIntent1)
                    .setSafetySourceId("custom_source_id")
                    .build(),
                SafetyCenterStaticEntry.Builder(staticEntry1)
                    .setSafetySourceId("custom_source_id")
                    .build(),
            )
            .addEqualityGroup(
                createSafetyCenterStaticEntryBuilder(title1, sourceId1, userId1)
                    .setSummary(summary1)
                    .setPendingIntent(pendingIntent1)
                    .setUser(UserHandle.of(5))
                    .build(),
                SafetyCenterStaticEntry.Builder(staticEntry1).setUser(UserHandle.of(5)).build(),
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntryMinimal)
                    .setSafetySourceId("source_id")
                    .setUser(UserHandle.of(5))
                    .build(),
                createSafetyCenterStaticEntryBuilder("", sourceId1, userId1)
                    .setSafetySourceId("source_id")
                    .setUser(UserHandle.of(5))
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntryMinimal)
                    .setUser(UserHandle.of(5))
                    .build(),
                createSafetyCenterStaticEntryBuilder("", sourceId1, userId1)
                    .setUser(UserHandle.of(5))
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntryMinimal)
                    .setSafetySourceId("source_id")
                    .build(),
                createSafetyCenterStaticEntryBuilder("", sourceId1, userId1)
                    .setSafetySourceId("source_id")
                    .build(),
            )
            .test()
    }

    @Test
    @ApiTest(
        apis =
            [
                "android.safetycenter.SafetyCenterStaticEntry#equals",
                "android.safetycenter.SafetyCenterStaticEntry#hashCode",
                "android.safetycenter.SafetyCenterStaticEntry#toString",
            ]
    )
    fun equalsHashCodeToString_usingEqualsHashCodeToStringTester() {
        EqualsHashCodeToStringTester.ofParcelable(
                parcelableCreator = SafetyCenterStaticEntry.CREATOR,
                createCopy = { SafetyCenterStaticEntry.Builder(it).build() },
            )
            .addEqualityGroup(
                staticEntry1,
                createSafetyCenterStaticEntryBuilder(title1, sourceId1, userId1)
                    .setSummary(summary1)
                    .setPendingIntent(pendingIntent1)
                    .build(),
            )
            .addEqualityGroup(staticEntry2)
            .addEqualityGroup(
                staticEntryMinimal,
                createSafetyCenterStaticEntryBuilder("", sourceId1, userId1).build(),
            )
            .addEqualityGroup(
                createSafetyCenterStaticEntryBuilder("titlee", sourceId1, userId1)
                    .setSummary("sumaree")
                    .setPendingIntent(pendingIntent1)
                    .build(),
                createSafetyCenterStaticEntryBuilder("titlee", sourceId1, userId1)
                    .setSummary("sumaree")
                    .setPendingIntent(pendingIntent1)
                    .build(),
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntry1).setTitle("a different title").build()
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntry1)
                    .setSummary("a different summary")
                    .build()
            )
            .addEqualityGroup(
                SafetyCenterStaticEntry.Builder(staticEntry1)
                    .setPendingIntent(pendingIntent2)
                    .build()
            )
            .test()
    }
}
