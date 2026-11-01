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
package android.app.role.cts

import android.app.role.RoleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Process
import android.util.Log
import com.android.compatibility.common.util.SystemUtil
import com.google.common.truth.Truth
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

private const val TIMEOUT_MILLIS = 15_000L

object RoleManagerUtil {
    private val TAG = RoleManagerUtil::class.java.simpleName

    /**
     * This method checks for the minimum screen size described in CDD {@see
     * https://source.android.com/docs/compatibility/14/android-14-cdd#7111_screen_size_and_shape}
     */
    fun isCddCompliantScreenSize(): Boolean {
        if (
            Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
                Configuration.UI_MODE_TYPE_WATCH
        ) {
            Log.d(TAG, "UI mode is UI_MODE_TYPE_WATCH, skipping the min dp check")
            return true
        }

        val screenSize =
            Resources.getSystem().configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK
        return when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> hasMinScreenSize(426, 320)
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> hasMinScreenSize(480, 320)
            Configuration.SCREENLAYOUT_SIZE_LARGE -> hasMinScreenSize(640, 480)
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> hasMinScreenSize(960, 720)
            else -> {
                Log.e(TAG, "Unknown screen size: $screenSize")
                true
            }
        }
    }

    private fun hasMinScreenSize(minWidthDp: Int, minHeightDp: Int): Boolean {
        val dpi = Resources.getSystem().displayMetrics.densityDpi
        val widthDp = (160f / dpi) * Resources.getSystem().displayMetrics.widthPixels
        val heightDp = (160f / dpi) * Resources.getSystem().displayMetrics.heightPixels

        // CDD does seem to follow width & height convention correctly, hence checking both ways
        return (widthDp >= minWidthDp && heightDp >= minHeightDp) ||
            (widthDp >= minHeightDp && heightDp >= minWidthDp)
    }

    /**
     * Adds a package as a role holder for the specified role.
     *
     * @param roleManager The RoleManager instance.
     * @param context The context to use for obtaining the main executor.
     * @param roleName The name of the role to add the holder to.
     * @param packageName The package name to add as a role holder.
     */
    fun addRoleHolder(
        roleManager: RoleManager,
        context: Context,
        roleName: String,
        packageName: String,
    ) {
        val future = CallbackFuture()
        SystemUtil.runWithShellPermissionIdentity {
            roleManager.addRoleHolderAsUser(
                roleName,
                packageName,
                0,
                Process.myUserHandle(),
                context.mainExecutor,
                future,
            )
        }
        Truth.assertThat(future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue()
    }

    /**
     * Removes a package as a role holder for the specified role.
     *
     * @param roleManager The RoleManager instance.
     * @param context The context to use for obtaining the main executor.
     * @param roleName The name of the role to remove the holder from.
     * @param packageName The package name to remove from role holders.
     */
    fun removeRoleHolder(
        roleManager: RoleManager,
        context: Context,
        roleName: String,
        packageName: String,
    ) {
        val future = CallbackFuture()
        SystemUtil.runWithShellPermissionIdentity {
            roleManager.removeRoleHolderAsUser(
                roleName,
                packageName,
                0,
                Process.myUserHandle(),
                context.mainExecutor,
                future,
            )
        }
        future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
    }

    /**
     * Retrieves the list of role holders for a specified role.
     *
     * @param roleManager The RoleManager instance.
     * @param roleName The name of the role to retrieve holders for.
     * @return A list of package names that are currently holding the role.
     */
    fun getRoleHolders(roleManager: RoleManager, roleName: String): List<String> {
        return SystemUtil.callWithShellPermissionIdentity<List<String>> {
            roleManager.getRoleHolders(roleName)
        }
    }
}

/**
 * A CompletableFuture that acts as a Consumer<Boolean>, used for receiving results from
 * asynchronous RoleManager operations.
 */
class CallbackFuture : CompletableFuture<Boolean>(), Consumer<Boolean> {
    override fun accept(successful: Boolean) {
        complete(successful)
    }
}
