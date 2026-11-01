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

package com.android.permissioncontroller.role.ui.wear

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Process
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.utils.Utils
import com.android.permissioncontroller.role.UserPackage
import com.android.permissioncontroller.role.model.UserDeniedManager
import com.android.permissioncontroller.role.ui.RequestRoleViewModel
import com.android.permissioncontroller.role.ui.RoleApplicationItem
import com.android.permissioncontroller.role.ui.wear.model.WearRequestRoleViewModel
import com.android.role.controller.model.Role
import java.util.Objects

/** A helper class for [WearRequestRoleScreen]. */
class WearRequestRoleHelper(
    val context: Context,
    val applicationInfo: ApplicationInfo,
    val role: Role,
    val roleName: String,
    val packageName: String,
    val viewModel: RequestRoleViewModel,
    val wearViewModel: WearRequestRoleViewModel,
) {
    fun getIcon() = Utils.getBadgedIcon(context, applicationInfo)

    fun getTitle() =
        context.getString(role.requestTitleResource, Utils.getAppLabel(applicationInfo, context))

    // Only show this button when the user denied once
    fun showDontAskButton() =
        UserDeniedManager.getInstance(context).isDeniedOnce(roleName, packageName)

    fun getNonePreference(
        applicationItems: List<RoleApplicationItem>,
        selectedPackage: UserPackage?,
    ): RequestRolePreference? =
        if (role.shouldShowNone()) {
            val hasHolderApplication = hasHolderApplication(applicationItems)
            RequestRolePreference(
                userPackage = null,
                label = context.getString(R.string.default_app_none),
                subTitle =
                    if (!hasHolderApplication) {
                        context.getString(R.string.request_role_current_default)
                    } else {
                        null
                    },
                icon = context.getDrawable(R.drawable.ic_remove_circle),
                checked = selectedPackage == null,
                enabled =
                    if (!wearViewModel.dontAskAgain()) {
                        true
                    } else {
                        !hasHolderApplication
                    },
                isHolder = !hasHolderApplication,
            )
        } else {
            null
        }

    fun getPreferences(
        applicationItems: List<RoleApplicationItem>,
        selectedPackage: UserPackage?,
    ): List<RequestRolePreference> {
        return applicationItems
            .map { applicationItem ->
                val userPackage = UserPackage.from(applicationItem.applicationInfo)
                RequestRolePreference(
                    userPackage = userPackage,
                    label = Utils.getAppLabel(applicationItem.applicationInfo, context),
                    subTitle =
                        if (applicationItem.isHolderApplication) {
                            context.getString(R.string.request_role_current_default)
                        } else {
                            context.getString(role.requestDescriptionResource)
                        },
                    icon = Utils.getBadgedIcon(context, applicationItem.applicationInfo),
                    checked = Objects.equals(userPackage, selectedPackage),
                    enabled =
                        if (!wearViewModel.dontAskAgain()) {
                            true
                        } else {
                            applicationItem.isHolderApplication
                        },
                    isHolder = applicationItem.isHolderApplication,
                )
            }
            .toList()
    }

    private fun hasHolderApplication(applicationItems: List<RoleApplicationItem>): Boolean =
        applicationItems.map { it.isHolderApplication }.contains(true)

    fun shouldSetAsDefaultEnabled(enabled: Boolean): Boolean {
        return enabled && (wearViewModel.dontAskAgain() || !wearViewModel.isHolderChecked)
    }

    fun initializeHolderPackage(applicationItems: List<RoleApplicationItem>) {
        wearViewModel.holderPackage =
            applicationItems
                .find { it.isHolderApplication }
                ?.applicationInfo
                ?.let { appInfo -> UserPackage.from(appInfo) }
    }

    fun initializeSelectedPackage() {
        if (wearViewModel.holderPackage == null) {
            wearViewModel.selectedPackage.value = null
        } else {
            wearViewModel.selectedPackage.value =
                UserPackage.of(Process.myUserHandle(), packageName)
        }
    }

    data class RequestRolePreference(
        val label: String,
        val subTitle: String?,
        val icon: Drawable?,
        val checked: Boolean,
        val enabled: Boolean,
        val userPackage: UserPackage?,
        val isHolder: Boolean,
    )
}
