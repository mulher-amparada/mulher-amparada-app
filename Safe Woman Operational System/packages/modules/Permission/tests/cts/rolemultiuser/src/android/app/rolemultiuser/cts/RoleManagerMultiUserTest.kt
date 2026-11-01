/*
 * Copyright (C) 2024 The Android Open Source Project
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
package android.app.rolemultiuser.cts

import android.app.Activity
import android.app.role.RoleManager
import android.app.role.cts.RoleManagerUtil
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_MANAGED_USERS
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager.DISALLOW_CONFIG_DEFAULT_APPS
import android.provider.Settings
import android.util.Log
import android.util.Pair
import androidx.test.filters.SdkSuppress
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import com.android.bedstead.enterprise.annotations.EnsureDoesNotHaveUserRestriction
import com.android.bedstead.enterprise.annotations.EnsureHasNoWorkProfile
import com.android.bedstead.enterprise.annotations.EnsureHasUserRestriction
import com.android.bedstead.enterprise.annotations.EnsureHasWorkProfile
import com.android.bedstead.enterprise.annotations.RequireRunOnWorkProfile
import com.android.bedstead.enterprise.workProfile
import com.android.bedstead.flags.annotations.RequireFlagsEnabled
import com.android.bedstead.harrier.BedsteadJUnit4
import com.android.bedstead.harrier.DeviceState
import com.android.bedstead.harrier.UserType.INITIAL_USER
import com.android.bedstead.harrier.UserType.WORK_PROFILE
import com.android.bedstead.harrier.annotations.RequireFeature
import com.android.bedstead.multiuser.annotations.EnsureHasAdditionalUser
import com.android.bedstead.multiuser.annotations.EnsureHasPrivateProfile
import com.android.bedstead.multiuser.annotations.EnsureHasSecondaryUser
import com.android.bedstead.multiuser.annotations.RequireRunNotOnSecondaryUser
import com.android.bedstead.multiuser.annotations.RequireRunOnPrimaryUser
import com.android.bedstead.multiuser.privateProfile
import com.android.bedstead.multiuser.secondaryUser
import com.android.bedstead.nene.TestApis.context
import com.android.bedstead.nene.TestApis.permissions
import com.android.bedstead.nene.TestApis.users
import com.android.bedstead.nene.types.OptionalBoolean
import com.android.bedstead.nene.userrestrictions.CommonUserRestrictions.DISALLOW_ADD_MANAGED_PROFILE
import com.android.bedstead.nene.users.UserReference
import com.android.bedstead.nene.users.UserType
import com.android.bedstead.permissions.CommonPermissions.INTERACT_ACROSS_USERS_FULL
import com.android.bedstead.permissions.CommonPermissions.MANAGE_DEFAULT_APPLICATIONS
import com.android.bedstead.permissions.CommonPermissions.MANAGE_ROLE_HOLDERS
import com.android.bedstead.permissions.annotations.EnsureDoesNotHavePermission
import com.android.bedstead.permissions.annotations.EnsureHasPermission
import com.android.compatibility.common.util.DisableAnimationRule
import com.android.compatibility.common.util.FreezeRotationRule
import com.android.compatibility.common.util.SystemUtil
import com.android.compatibility.common.util.SystemUtil.eventually
import com.android.compatibility.common.util.UiAutomatorUtils2.getUiDevice
import com.android.compatibility.common.util.UiAutomatorUtils2.waitFindObject
import com.android.compatibility.common.util.UiAutomatorUtils2.waitFindObjectOrNull
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
@RunWith(BedsteadJUnit4::class)
class RoleManagerMultiUserTest {

    @JvmField
    @Rule
    var activityRule: ActivityTestRule<WaitForResultActivity> =
        ActivityTestRule(WaitForResultActivity::class.java)

    @Before
    fun setUp() {
        assumeTrue(RoleManagerUtil.isCddCompliantScreenSize())
        installAppForAllUsers()

        // If "none" selected in test, ensure we re-enable fallback for other test runs
        permissions().withPermission(MANAGE_ROLE_HOLDERS, INTERACT_ACROSS_USERS_FULL).use {
            setRoleFallbackEnabledForAllUsers()
        }
    }

    @After
    fun tearDown() {
        uninstallAppForAllUsers()

        // If "none" selected in test, ensure we re-enable fallback for other test runs
        permissions().withPermission(MANAGE_ROLE_HOLDERS, INTERACT_ACROSS_USERS_FULL).use {
            setRoleFallbackEnabledForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @EnsureHasPrivateProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(Exception::class)
    fun isAvailableAsUserForProfileGroupExclusiveRole() {
        val workProfileRoleManager = getRoleManagerForUser(deviceState.workProfile())
        val privateProfileRoleManager = getRoleManagerForUser(deviceState.privateProfile())

        assertThat(roleManager.isRoleAvailable(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)).isTrue()
        assertThat(workProfileRoleManager.isRoleAvailable(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isTrue()
        assertThat(privateProfileRoleManager.isRoleAvailable(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isFalse()
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @Test
    @Throws(Exception::class)
    fun cannotGetActiveUserForNonCrossUserRole() {
        assertThrows(IllegalArgumentException::class.java) {
            roleManager.getActiveUserForRole(RoleManager.ROLE_SYSTEM_ACTIVITY_RECOGNIZER)
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(MANAGE_ROLE_HOLDERS)
    @EnsureDoesNotHavePermission(INTERACT_ACROSS_USERS_FULL)
    @Test
    @Throws(Exception::class)
    fun cannotGetActiveUserForRoleWithoutInteractAcrossUserPermission() {
        assertThrows(SecurityException::class.java) {
            roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL)
    @EnsureDoesNotHavePermission(MANAGE_ROLE_HOLDERS, MANAGE_DEFAULT_APPLICATIONS)
    @Test
    @Throws(Exception::class)
    fun cannotGetActiveUserForRoleWithoutManageRoleAndManageDefaultApplicationsPermission() {
        assertThrows(SecurityException::class.java) {
            roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForNonCrossUserRole() {
        assertThrows(IllegalArgumentException::class.java) {
            roleManager.setActiveUserForRole(
                RoleManager.ROLE_SYSTEM_ACTIVITY_RECOGNIZER,
                Process.myUserHandle(),
                0,
            )
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(MANAGE_ROLE_HOLDERS)
    @EnsureDoesNotHavePermission(INTERACT_ACROSS_USERS_FULL)
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForRoleWithoutInteractAcrossUserPermission() {
        assertThrows(SecurityException::class.java) {
            roleManager.setActiveUserForRole(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                Process.myUserHandle(),
                0,
            )
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL)
    @EnsureDoesNotHavePermission(MANAGE_ROLE_HOLDERS, MANAGE_DEFAULT_APPLICATIONS)
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForRoleWithoutManageRoleAndManageDefaultApplicationsPermission() {
        assertThrows(SecurityException::class.java) {
            roleManager.setActiveUserForRole(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                Process.myUserHandle(),
                0,
            )
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForRoleToNonExistentUser() {
        val targetActiveUser = users().nonExisting().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, targetActiveUser, 0)

        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isNotEqualTo(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasPrivateProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForRoleToPrivateProfileUser() {
        val targetActiveUser = deviceState.privateProfile().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, targetActiveUser, 0)

        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isNotEqualTo(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasAdditionalUser(installInstrumentedApp = OptionalBoolean.TRUE)
    @EnsureHasSecondaryUser
    @RequireRunNotOnSecondaryUser
    @Test
    @Throws(Exception::class)
    fun cannotSetActiveUserForRoleToUserNotInProfileGroup() {
        val targetActiveUser = deviceState.secondaryUser().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, targetActiveUser, 0)

        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isNotEqualTo(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @EnsureHasAdditionalUser(installInstrumentedApp = OptionalBoolean.TRUE)
    @EnsureHasSecondaryUser
    @RequireRunNotOnSecondaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun ensureRoleHasActiveUser() {
        val primaryUser = deviceState.initialUser()
        val primaryUserId = primaryUser.userHandle().identifier
        val primaryUserRoleManager = getRoleManagerForUser(primaryUser)
        val secondaryUser = deviceState.secondaryUser()
        val secondaryUserId = secondaryUser.userHandle().identifier
        val secondaryUserRoleManager = getRoleManagerForUser(secondaryUser)

        assertWithMessage("Expected active user in profile group for user $primaryUserId")
            .that(primaryUserRoleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isNotNull()
        assertWithMessage("Expected active user in profile group for user $secondaryUserId")
            .that(
                secondaryUserRoleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            )
            .isNotNull()
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureDoesNotHavePermission(MANAGE_DEFAULT_APPLICATIONS)
    @EnsureHasWorkProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @Test
    @Throws(Exception::class)
    fun setAndGetActiveUserForRoleSetCurrentUserWithManageRoleHoldersPermission() {
        assumeFalse(
            "setActiveUser not supported for private profile",
            users().current().type().name() == PRIVATE_PROFILE_TYPE_NAME,
        )

        val targetActiveUser = users().current().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, targetActiveUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_DEFAULT_APPLICATIONS)
    @EnsureDoesNotHavePermission(MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @Test
    @Throws(Exception::class)
    fun setAndGetActiveUserForRoleSetCurrentUserWithManageDefaultApplicationPermission() {
        assumeFalse(
            "setActiveUser not supported for private profile",
            users().current().type().name() == PRIVATE_PROFILE_TYPE_NAME,
        )

        val targetActiveUser = users().current().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, targetActiveUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @Test
    @Throws(Exception::class)
    fun setAndGetActiveUserForRoleSetCurrentUserEnsureRoleNotHeldByInactiveUser() {
        assumeFalse(
            "setActiveUser not supported for private profile",
            users().current().type().name() == PRIVATE_PROFILE_TYPE_NAME,
        )
        // initialUser needs to be not the targetUser
        val targetActiveUser = users().current().userHandle()
        val initialUser =
            if (targetActiveUser == deviceState.initialUser().userHandle()) {
                deviceState.workProfile().userHandle()
            } else {
                deviceState.initialUser().userHandle()
            }
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)

        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()

            roleManager.setActiveUserForRole(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                targetActiveUser,
                0,
            )
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            // We can assume targetActiveUser is role holder since fallback is enabled
            eventually { assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser) }
        } finally {
            clearDefaultHoldersForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile(installInstrumentedApp = OptionalBoolean.TRUE)
    @Test
    @Throws(Exception::class)
    fun setAndGetActiveUserForRoleSetWorkProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()

            val targetActiveUser = deviceState.workProfile().userHandle()
            roleManager.setActiveUserForRole(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                targetActiveUser,
                0,
            )

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            // We can assume targetActiveUser is role holder since fallback is enabled
            eventually { assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser) }
        } finally {
            setDefaultHoldersForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(MANAGE_ROLE_HOLDERS)
    @EnsureDoesNotHavePermission(INTERACT_ACROSS_USERS_FULL)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(Exception::class)
    fun cannotAddRoleHolderAsUserForProfileExclusiveRoleWithoutInteractAcrossUserPermission() {
        // Set other user as active
        val initialUser = deviceState.workProfile().userHandle()
        // setActiveUserForRole and getActiveUserForRole is used to ensure initial active users
        // state and requires INTERACT_ACROSS_USERS_FULL
        permissions().withPermission(INTERACT_ACROSS_USERS_FULL).use {
            roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialUser)
        }

        val targetActiveUser = users().current().userHandle()
        val future = CallbackFuture()
        assertThrows(SecurityException::class.java) {
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                targetActiveUser,
                context.mainExecutor,
                future,
            )
        }
        assertThat(
                roleManager.getRoleHoldersAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    targetActiveUser,
                )
            )
            .isEmpty()

        // getActiveUserForRole is used to ensure addRoleHolderAsUser didn't set active user, and
        // requires INTERACT_ACROSS_USERS_FULL
        permissions().withPermission(INTERACT_ACROSS_USERS_FULL).use {
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialUser)
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun addRoleHolderAsUserSetsCurrentUserAsActive() {
        // Set other user as active
        val initialUser = deviceState.workProfile().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val targetActiveUser = users().current().userHandle()
        val future = CallbackFuture()
        roleManager.addRoleHolderAsUser(
            PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
            APP_PACKAGE_NAME,
            0,
            targetActiveUser,
            context.mainExecutor,
            future,
        )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
        assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun addRoleHolderAsUserSetsWorkProfileAsActive() {
        // Set other user as active
        val initialUser = deviceState.initialUser().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val targetActiveUser = deviceState.workProfile().userHandle()

        assertThat(targetActiveUser).isNotEqualTo(initialUser)
        val future = CallbackFuture()
        roleManager.addRoleHolderAsUser(
            PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
            APP_PACKAGE_NAME,
            0,
            targetActiveUser,
            context.mainExecutor,
            future,
        )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
        assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun addRoleHolderAsUserReenablesFallbackOnProfileParent() {
        // Set other user as active
        val initialUserReference = deviceState.initialUser()
        val initialUser = initialUserReference.userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val profileParentRoleManager = getRoleManagerForUser(initialUserReference)
        profileParentRoleManager.setRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, false)
        assertThat(
                profileParentRoleManager.isRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            )
            .isFalse()

        val targetActiveUser = deviceState.workProfile().userHandle()
        val future = CallbackFuture()
        roleManager.addRoleHolderAsUser(
            PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
            APP_PACKAGE_NAME,
            0,
            targetActiveUser,
            context.mainExecutor,
            future,
        )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(
                profileParentRoleManager.isRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            )
            .isTrue()
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(MANAGE_DEFAULT_APPLICATIONS)
    @EnsureDoesNotHavePermission(INTERACT_ACROSS_USERS_FULL)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(Exception::class)
    fun cannotSetDefaultApplicationForProfileExclusiveRoleWithoutInteractAcrossUserPermission() {
        // Set other user as active
        val initialUser = deviceState.workProfile().userHandle()
        // setActiveUserForRole and getActiveUserForRole is used to ensure initial active users
        // state and requires INTERACT_ACROSS_USERS_FULL
        permissions().withPermission(INTERACT_ACROSS_USERS_FULL).use {
            roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialUser)
        }

        val future = CallbackFuture()
        assertThrows(SecurityException::class.java) {
            roleManager.setDefaultApplication(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                context.mainExecutor,
                future,
            )
        }
        assertThat(roleManager.getDefaultApplication(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)).isNull()

        // getActiveUserForRole is used to ensure setDefaultApplication didn't set active user,
        // and requires INTERACT_ACROSS_USERS_FULL
        permissions().withPermission(INTERACT_ACROSS_USERS_FULL).use {
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialUser)
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_DEFAULT_APPLICATIONS)
    @EnsureHasWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun setDefaultApplicationSetsCurrentUserAsActive() {
        // Set other user as active
        val initialUser = deviceState.workProfile().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val targetActiveUser = users().current().userHandle()
        val future = CallbackFuture()
        roleManager.setDefaultApplication(
            PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
            APP_PACKAGE_NAME,
            0,
            context.mainExecutor,
            future,
        )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
        eventually { assertExpectedProfileHasRoleUsingGetDefaultApplication(targetActiveUser) }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_DEFAULT_APPLICATIONS)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun setDefaultApplicationSetsWorkProfileAsActive() {
        // Set other user as active
        val initialUser = deviceState.initialUser().userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val targetActiveUser = deviceState.workProfile().userHandle()
        assertThat(targetActiveUser).isNotEqualTo(initialUser)
        val future = CallbackFuture()
        roleManager.setDefaultApplication(
            PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
            APP_PACKAGE_NAME,
            0,
            context.mainExecutor,
            future,
        )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(targetActiveUser)
        eventually { assertExpectedProfileHasRoleUsingGetDefaultApplication(targetActiveUser) }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(
        INTERACT_ACROSS_USERS_FULL,
        MANAGE_DEFAULT_APPLICATIONS,
        MANAGE_ROLE_HOLDERS,
    )
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun setDefaultApplicationReenablesFallbackOnProfileParent() {
        // Set other user as active
        val initialUserReference = deviceState.initialUser()
        val initialUser = initialUserReference.userHandle()
        roleManager.setActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, initialUser, 0)
        assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
            .isEqualTo(initialUser)

        val profileParentRoleManager = getRoleManagerForUser(initialUserReference)
        profileParentRoleManager.setRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, false)
        assertThat(
                profileParentRoleManager.isRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            )
            .isFalse()

        val future = CallbackFuture()
        getRoleManagerForUser(deviceState.workProfile())
            .setDefaultApplication(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                context.mainExecutor,
                future,
            )
        assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(
                profileParentRoleManager.isRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            )
            .isTrue()
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    // TODO(b/442891661) Check that Managed Profile is supported?
    @EnsureHasNoWorkProfile
    @RequireRunOnPrimaryUser
    @EnsureDoesNotHaveUserRestriction(DISALLOW_ADD_MANAGED_PROFILE)
    @RequireFeature(FEATURE_MANAGED_USERS)
    @Test
    @Throws(Exception::class)
    fun ensureActiveUserSetToParentOnUserRemoved() {
        users()
            .createUser()
            .parent(users().initial())
            .type(users().supportedType(UserType.MANAGED_PROFILE_TYPE_NAME))
            .createAndStart()
            .use { userReference ->
                val targetActiveUser = userReference.userHandle()
                roleManager.setActiveUserForRole(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    targetActiveUser,
                    0,
                )
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)

                userReference.remove()
            }

        // Removal of users in roles service might take a moment
        eventually {
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(users().current().userHandle())
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetDefaultAppThenIsDefaultApp() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
            }

            if (isWatch) {
                waitFindObject(
                    By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel))
                )
            } else {
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetWorkDefaultAppThenIsDefaultApp() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
            }

            if (isWatch) {
                waitFindObject(
                    By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel))
                )
            } else {
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetDefaultAppThenSetNoneThenHasNoneDefaultApp() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                getUiDevice().waitForIdle()
                waitFindObject(By.clickable(true).hasDescendant(By.text(NONE_LABEL))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                getUiDevice().waitForIdle()
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(NONE_LABEL))
                    )
                    .click()
            }

            if (isWatch) {
                waitFindObject(By.clickable(true).checked(true).hasDescendant(By.text(NONE_LABEL)))
            } else {
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(NONE_LABEL))
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(deviceState.initialUser().userHandle())
            assertNoRoleHoldersUsingGetRoleHoldersAsUser()

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetWorkDefaultAppThenSetNoneThenHasNoneDefaultApp() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                getUiDevice().waitForIdle()
                waitFindObject(By.clickable(true).hasDescendant(By.text(NONE_LABEL))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                getUiDevice().waitForIdle()
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(NONE_LABEL))
                    )
                    .click()
            }

            if (isWatch) {
                waitFindObject(By.clickable(true).checked(true).hasDescendant(By.text(NONE_LABEL)))
            } else {
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(NONE_LABEL))
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(deviceState.initialUser().userHandle())
            assertNoRoleHoldersUsingGetRoleHoldersAsUser()

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetDefaultAppThenIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                waitFindObject(
                    By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel))
                )
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
            }
            pressBack()

            waitFindObject(By.text(targetAppLabel))

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetWorkDefaultAppThenIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                waitFindObject(
                    By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel))
                )
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
            }
            pressBack()

            waitFindObject(By.text(targetAppLabel))

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetDefaultAppThenSetNoneThenIsNoneDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                getUiDevice().waitForIdle()
                waitFindObject(By.clickable(true).hasDescendant(By.text(NONE_LABEL))).click()
                waitFindObject(By.clickable(true).checked(true).hasDescendant(By.text(NONE_LABEL)))
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                getUiDevice().waitForIdle()
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(NONE_LABEL))
                    )
                    .click()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(NONE_LABEL))
                )
            }
            pressBack()

            waitFindObject(
                By.clickable(true)
                    .hasDescendant(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL))
                    .hasDescendant(By.text(NONE_LABEL))
            )

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(deviceState.initialUser().userHandle())
            assertNoRoleHoldersUsingGetRoleHoldersAsUser()

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndSetWorkDefaultAppThenSetNoneThenIsNoneDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                getUiDevice().waitForIdle()
                waitFindObject(By.clickable(true).hasDescendant(By.text(NONE_LABEL))).click()
                waitFindObject(By.clickable(true).checked(true).hasDescendant(By.text(NONE_LABEL)))
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                getUiDevice().waitForIdle()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
                getUiDevice().waitForIdle()
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(NONE_LABEL))
                    )
                    .click()
                getUiDevice().waitForIdle()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(NONE_LABEL))
                )
            }
            pressBack()

            waitFindObject(
                By.clickable(true)
                    .hasDescendant(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL))
                    .hasDescendant(By.text(NONE_LABEL))
            )

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(deviceState.initialUser().userHandle())
            assertNoRoleHoldersUsingGetRoleHoldersAsUser()

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListFromPrimaryUserAndShowsPrimaryIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            val targetActiveUser = deviceState.initialUser().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                targetActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            waitFindObject(By.text(targetAppLabel))

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListFromPrimaryUserAndShowsWorkIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                targetActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            waitFindObject(By.text(targetAppLabel))

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListFromWorkProfileAndShowsPrimaryIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            val targetActiveUser = deviceState.initialUser().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                targetActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            waitFindObject(By.text(targetAppLabel))

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListFromWorkProfileAndShowsWorkIsDefaultAppInList() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            val targetActiveUser = deviceState.workProfile().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                targetActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            waitFindObject(By.text(targetAppLabel))

            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleAndAllowPrimaryThenIsRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)

            val targetActiveUser = deviceState.initialUser().userHandle()
            respondToRoleRequest(true, targetActiveUser)

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)
                assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleAndAllowWorkThenWorkIsRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)

            val targetActiveUser = deviceState.workProfile().userHandle()
            respondToRoleRequest(true, targetActiveUser)

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)
                assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleAndSelectNoneThenIsNoneRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            respondNoneToRoleRequest()

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(deviceState.initialUser().userHandle())
                assertNoRoleHoldersUsingGetRoleHoldersAsUser()
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleFromWorkProfileAndAllowPrimaryThenIsRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-work selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.initialUser().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)

            val targetActiveUser = deviceState.initialUser().userHandle()
            respondToRoleRequest(true, targetActiveUser)

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)
                assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleFromWorkProfileAndAllowWorkThenWorkIsRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-work selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.initialUser().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)

            val targetActiveUser = deviceState.workProfile().userHandle()
            respondToRoleRequest(true, targetActiveUser)

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)
                assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED)
    @EnsureHasWorkProfile
    @RequireRunOnWorkProfile
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleFromWorkProfileAndSelectNoneThenIsNoneRoleHolder() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-work selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.initialUser().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            respondNoneToRoleRequest()

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(deviceState.initialUser().userHandle())
                assertNoRoleHoldersUsingGetRoleHoldersAsUser()
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = false)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndOpenDefaultAppWhenBYODHasUserRestrictionOnPrimaryProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            // CollapsingToolbar title can't be found by text, so using description instead.
            waitFindObject(By.desc(PROFILE_GROUP_EXCLUSIVITY_ROLE_LABEL))

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndCannotOpenDefaultAppWhenHasUserRestrictionOnPrimaryProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            // CollapsingToolbar title can't be found by text, so using description instead.
            assertNull(
                waitFindObjectOrNull(
                    By.desc(PROFILE_GROUP_EXCLUSIVITY_ROLE_LABEL),
                    IDLE_TIMEOUT_MILLIS,
                )
            )

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = WORK_PROFILE)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppListAndCannotOpenDefaultAppWhenHasUserRestrictionOnWorkProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()
            waitFindObject(By.text(PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL)).click()
            getUiDevice().waitForIdle()

            // CollapsingToolbar title can't be found by text, so using description instead.
            assertNull(
                waitFindObjectOrNull(
                    By.desc(PROFILE_GROUP_EXCLUSIVITY_ROLE_LABEL),
                    IDLE_TIMEOUT_MILLIS,
                )
            )

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = false)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppDetailsAndSetDefaultAppWhenBYODHasUserRestrictionOnPrimaryProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            // Ensure non-target selected first. Request exits early if user and package
            // already the role holder
            val initialActiveUser = deviceState.workProfile().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                initialActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()

            context.startActivity(
                Intent(Intent.ACTION_MANAGE_DEFAULT_APP)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(Intent.EXTRA_ROLE_NAME, PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
                waitFindObject(
                    By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel))
                )
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
                waitFindObject(
                    By.clickable(true)
                        .hasDescendant(By.checkable(true).checked(true))
                        .hasDescendant(By.text(targetAppLabel))
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(targetActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppDetailsAndCannotSetDefaultAppWhenHasUserRestrictionOnPrimaryProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            // Ensure non-target selected first. Request exits early if user and package
            // already the role holder
            val initialActiveUser = deviceState.workProfile().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                initialActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()

            context.startActivity(
                Intent(Intent.ACTION_MANAGE_DEFAULT_APP)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(Intent.EXTRA_ROLE_NAME, PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
            }

            if (isWatch) {
                assertNull(
                    waitFindObjectOrNull(
                        By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel)),
                        IDLE_TIMEOUT_MILLIS,
                    )
                )
            } else {
                assertNull(
                    waitFindObjectOrNull(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true).checked(true))
                            .hasDescendant(By.text(targetAppLabel)),
                        IDLE_TIMEOUT_MILLIS,
                    )
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(initialActiveUser)

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS)
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = WORK_PROFILE)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun openDefaultAppDetailsAndCannotSetDefaultAppWhenHasUserRestrictionOnWorkProfile() {
        try {
            // Set test default role holder. Ensures fallbacks to a default holder
            setDefaultHoldersForTestForAllUsers()
            setRoleVisibleForTestForAllUsers()

            // Ensure non-target selected first. Request exits early if user and package
            // already the role holder
            val initialActiveUser = deviceState.workProfile().userHandle()
            val future = CallbackFuture()
            roleManager.addRoleHolderAsUser(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                APP_PACKAGE_NAME,
                0,
                initialActiveUser,
                context.mainExecutor,
                future,
            )
            assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()

            context.startActivity(
                Intent(Intent.ACTION_MANAGE_DEFAULT_APP)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(Intent.EXTRA_ROLE_NAME, PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            getUiDevice().waitForIdle()

            val targetActiveUser = users().current().userHandle()
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            if (isWatch) {
                waitFindObject(By.clickable(true).hasDescendant(By.text(targetAppLabel))).click()
            } else {
                waitFindObject(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true))
                            .hasDescendant(By.text(targetAppLabel))
                    )
                    .click()
            }

            if (isWatch) {
                assertNull(
                    waitFindObjectOrNull(
                        By.clickable(true).checked(true).hasDescendant(By.text(targetAppLabel)),
                        IDLE_TIMEOUT_MILLIS,
                    )
                )
            } else {
                assertNull(
                    waitFindObjectOrNull(
                        By.clickable(true)
                            .hasDescendant(By.checkable(true).checked(true))
                            .hasDescendant(By.text(targetAppLabel)),
                        IDLE_TIMEOUT_MILLIS,
                    )
                )
            }

            assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                .isEqualTo(initialActiveUser)
            assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(initialActiveUser)

            pressBack()
            pressBack()
        } finally {
            clearDefaultHoldersForTestForAllUsers()
            clearRoleVisibleForTestForAllUsers()
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = false)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleAllowedWhenBYODHasUserRestrictionOnPrimaryProfile() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)

            val targetActiveUser = deviceState.initialUser().userHandle()
            respondToRoleRequest(true, targetActiveUser)

            // getActiveUserForRole and getRoleHoldersAsUser require INTERACT_ACROSS_USERS_FULL and
            // MANAGE_ROLE_HOLDERS permissions to validate cross user role active user and role
            // holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                assertThat(roleManager.getActiveUserForRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME))
                    .isEqualTo(targetActiveUser)
                assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(targetActiveUser)
            }
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = INITIAL_USER)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleDeniedWhenHasUserRestrictionOnPrimaryProfile() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            roleRequestNotShown()
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    @RequireFlagsEnabled(
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_ENABLED,
        com.android.permission.flags.Flags.FLAG_CROSS_USER_ROLE_UX_BUGFIX_ENABLED,
    )
    @EnsureHasUserRestriction(value = DISALLOW_CONFIG_DEFAULT_APPS, onUser = WORK_PROFILE)
    @EnsureHasWorkProfile(isOrganizationOwned = true)
    @RequireRunOnPrimaryUser
    @Test
    @Throws(java.lang.Exception::class)
    fun requestRoleDeniedWhenHasUserRestrictionOnWorkProfile() {
        try {
            // setDefaultHoldersForTestForAllUsers and setRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                // Set test default role holder. Ensures fallbacks to a default holder
                setDefaultHoldersForTestForAllUsers()
                setRoleVisibleForTestForAllUsers()

                // Ensure non-primary selected first. Request exits early if user and package
                // already the role holder
                val future = CallbackFuture()
                roleManager.addRoleHolderAsUser(
                    PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                    APP_PACKAGE_NAME,
                    0,
                    deviceState.workProfile().userHandle(),
                    context.mainExecutor,
                    future,
                )
                assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
            }

            requestRole(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
            roleRequestNotShown()
        } finally {
            // clearDefaultHoldersForTestForAllUsers and clearRoleVisibleForTestForAllUsers require
            // INTERACT_ACROSS_USERS_FULL and MANAGE_ROLE_HOLDERS permissions to validate cross user
            // role active user and role holder states
            permissions().withPermission(INTERACT_ACROSS_USERS_FULL, MANAGE_ROLE_HOLDERS).use {
                clearDefaultHoldersForTestForAllUsers()
                clearRoleVisibleForTestForAllUsers()
            }
        }
    }

    private fun installAppForAllUsers() {
        SystemUtil.runShellCommandOrThrow("pm install -r --user all $APP_APK_PATH")
    }

    private fun uninstallAppForAllUsers() {
        SystemUtil.runShellCommand("pm uninstall $APP_PACKAGE_NAME")
    }

    private fun pressBack() {
        getUiDevice().pressBack()
        getUiDevice().waitForIdle()
    }

    private fun requestRole(roleName: String) {
        val intent =
            Intent()
                .setComponent(ComponentName(APP_PACKAGE_NAME, APP_REQUEST_ROLE_ACTIVITY_NAME))
                .putExtra(Intent.EXTRA_ROLE_NAME, roleName)
        activityRule.getActivity().startActivityToWaitForResult(intent)
    }

    private fun respondToRoleRequest(allow: Boolean, targetActiveUser: UserHandle) {
        if (allow) {
            val targetAppLabel = "$APP_LABEL@${targetActiveUser.identifier}"
            waitFindObject(By.text(targetAppLabel)).click()
        }
        val result: Pair<Int, Intent?> = clickButtonAndWaitForResult(allow)
        val expectedResult =
            if (allow && targetActiveUser == users().instrumented().userHandle()) Activity.RESULT_OK
            else Activity.RESULT_CANCELED

        assertThat(result.first).isEqualTo(expectedResult)
    }

    private fun respondNoneToRoleRequest() {
        waitFindObject(By.text(NONE_LABEL)).click()
        val result: Pair<Int, Intent?> = clickButtonAndWaitForResult(true)
        assertThat(result.first).isEqualTo(Activity.RESULT_CANCELED)
    }

    private fun clickButtonAndWaitForResult(positive: Boolean): Pair<Int, Intent?> {
        waitFindObject(if (positive) POSITIVE_BUTTON_SELECTOR else NEGATIVE_BUTTON_SELECTOR).click()
        return waitForResult()
    }

    private fun roleRequestNotShown() {
        val requestRoleItem =
            waitFindObjectOrNull(By.textStartsWith(APP_LABEL), IDLE_TIMEOUT_MILLIS)
        assertNull(requestRoleItem)

        val result: Pair<Int, Intent?> = waitForResult()
        assertThat(result.first).isEqualTo(Activity.RESULT_CANCELED)
    }

    @Throws(InterruptedException::class)
    private fun waitForResult(): Pair<Int, Intent?> {
        return activityRule.getActivity().waitForActivityResult(TIMEOUT_MILLIS)
    }

    private fun assertNoRoleHoldersUsingGetRoleHoldersAsUser() {
        for (userReference in users().profileGroup(deviceState.initialUser())) {
            val user = userReference.userHandle()
            // Verify the non-active user does not hold the role
            assertWithMessage(
                    "Expected user ${user.identifier} to not have a role holder for" +
                        " $PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME"
                )
                .that(roleManager.getRoleHoldersAsUser(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, user))
                .isEmpty()
        }
    }

    private fun assertExpectedProfileHasRoleUsingGetRoleHoldersAsUser(
        expectedActiveUser: UserHandle
    ) {
        for (userReference in users().profileGroup(deviceState.initialUser())) {
            val user = userReference.userHandle()
            val roleHolders =
                roleManager.getRoleHoldersAsUser(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, user)
            if (user == expectedActiveUser) {
                assertWithMessage(
                        "Expected user ${user.identifier} to have a role holder for " +
                            " $PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME"
                    )
                    .that(roleHolders)
                    .isNotEmpty()
                assertWithMessage(
                        "Expected user ${user.identifier} to have $APP_PACKAGE_NAME as role " +
                            "holder for $PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME"
                    )
                    .that(roleHolders.first())
                    .isEqualTo(APP_PACKAGE_NAME)
            } else {
                // Verify the non-active user does not hold the role
                assertWithMessage(
                        "Expected user ${user.identifier} to not have a role holder for" +
                            " $PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME"
                    )
                    .that(roleHolders)
                    .isEmpty()
            }
        }
    }

    private fun assertExpectedProfileHasRoleUsingGetDefaultApplication(
        expectedActiveUser: UserHandle
    ) {
        for (userReference in users().profileGroup(deviceState.initialUser())) {
            val userRoleManager = getRoleManagerForUser(userReference)
            val user = userReference.userHandle()
            if (user == expectedActiveUser) {
                assertWithMessage("Expected default application for user ${user.identifier}")
                    .that(
                        userRoleManager.getDefaultApplication(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
                    )
                    .isEqualTo(APP_PACKAGE_NAME)
            } else {
                // Verify the non-active user does not hold the role
                assertWithMessage("Expected no default application for user ${user.identifier}")
                    .that(
                        userRoleManager.getDefaultApplication(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME)
                    )
                    .isNull()
            }
        }
    }

    private fun setDefaultHoldersForTestForAllUsers() {
        // Set test default role holder. Ensures fallbacks to a default holder
        for (userRoleManager in
            users().profileGroup(users().current()).map { getRoleManagerForUser(it) }) {
            userRoleManager.setDefaultHoldersForTest(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                listOf(APP_PACKAGE_NAME),
            )
        }
    }

    private fun clearDefaultHoldersForTestForAllUsers() {
        // Set test default role holder. Ensures fallbacks to a default holder
        for (userRoleManager in
            users().profileGroup(users().current()).map { getRoleManagerForUser(it) }) {
            userRoleManager.setDefaultHoldersForTest(
                PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME,
                emptyList(),
            )
        }
    }

    private fun setRoleVisibleForTestForAllUsers() {
        // Set test default role holder. Ensures fallbacks to a default holder
        for (userRoleManager in
            users().profileGroup(users().current()).map { getRoleManagerForUser(it) }) {
            userRoleManager.setRoleVisibleForTest(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, true)
        }
    }

    private fun clearRoleVisibleForTestForAllUsers() {
        // Set test default role holder. Ensures fallbacks to a default holder
        for (userRoleManager in
            users().profileGroup(users().current()).map { getRoleManagerForUser(it) }) {
            userRoleManager.setRoleVisibleForTest(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, false)
        }
    }

    private fun setRoleFallbackEnabledForAllUsers() {
        for (userReference in users().profileGroup(users().current())) {
            try {
                val userRoleManager = getRoleManagerForUser(userReference)
                userRoleManager.setRoleFallbackEnabled(PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME, true)
            } catch (e: Exception) {
                Log.w(
                    LOG_TAG,
                    "Encountered error setting fallback enabled for" +
                        " $PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME@" +
                        "${userReference.userHandle().identifier}",
                    e,
                )
            }
        }
    }

    private fun getRoleManagerForUser(user: UserReference): RoleManager {
        val userContext = context().androidContextAsUser(user)
        return userContext.getSystemService(RoleManager::class.java)
    }

    class CallbackFuture : CompletableFuture<Boolean?>(), Consumer<Boolean?> {
        override fun accept(successful: Boolean?) {
            complete(successful)
        }
    }

    companion object {
        private val LOG_TAG = RoleManagerMultiUserTest::class.java.simpleName

        private const val TIMEOUT_MILLIS: Long = (15 * 1000).toLong()
        private const val IDLE_TIMEOUT_MILLIS: Long = (2 * 1000).toLong()
        private const val PROFILE_GROUP_EXCLUSIVITY_ROLE_NAME =
            RoleManager.ROLE_RESERVED_FOR_TESTING_PROFILE_GROUP_EXCLUSIVITY
        private const val PROFILE_GROUP_EXCLUSIVITY_ROLE_LABEL =
            "Default test profile group exclusive role app"
        private const val PROFILE_GROUP_EXCLUSIVITY_ROLE_SHORT_LABEL =
            "Test profile group exclusive role app"
        private const val PRIVATE_PROFILE_TYPE_NAME = "android.os.usertype.profile.PRIVATE"
        private const val APP_APK_PATH = "/data/local/tmp/cts-role/CtsRoleTestApp.apk"
        private const val APP_PACKAGE_NAME = "android.app.role.cts.app"
        private const val APP_LABEL = "CtsRoleTestApp"
        private const val APP_REQUEST_ROLE_ACTIVITY_NAME = APP_PACKAGE_NAME + ".RequestRoleActivity"
        private const val NONE_LABEL = "None"

        private val context: Context = context().instrumentedContext()
        private val roleManager: RoleManager = context.getSystemService(RoleManager::class.java)
        private val packageManager: PackageManager = context.packageManager
        private val isWatch = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

        private val NEGATIVE_BUTTON_SELECTOR =
            if (isWatch) By.text("Cancel") else By.res("android:id/button2")
        private val POSITIVE_BUTTON_SELECTOR =
            if (isWatch) By.text("Set as default") else By.res("android:id/button1")

        @JvmField @ClassRule @Rule val deviceState = DeviceState()

        @JvmField
        @ClassRule
        @Rule
        var disableAnimationRule: DisableAnimationRule = DisableAnimationRule()

        @JvmField @ClassRule @Rule var freezeRotationRule: FreezeRotationRule = FreezeRotationRule()
    }
}
