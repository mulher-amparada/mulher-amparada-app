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
import android.os.UserHandle
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.utils.Utils
import com.android.permissioncontroller.role.ui.DefaultAppViewModel
import com.android.permissioncontroller.role.ui.RoleApplicationItem
import com.android.permissioncontroller.role.ui.behavior.ConfirmationDialogInfo
import com.android.permissioncontroller.role.ui.wear.model.ConfirmDialogArgs
import com.android.permissioncontroller.role.ui.wear.model.DefaultAppConfirmDialogViewModel
import com.android.permissioncontroller.role.utils.RoleUiBehaviorUtils
import com.android.role.controller.model.Role

/** A helper class to retrieve default apps to [WearDefaultAppScreen]. */
class WearDefaultAppHelper(
    val context: Context,
    val user: UserHandle,
    val role: Role,
    val viewModel: DefaultAppViewModel,
    val confirmDialogViewModel: DefaultAppConfirmDialogViewModel,
) {
    fun getTitle() = context.getString(role.labelResource)

    fun getNonePreference(
        recommendedItems: List<RoleApplicationItem>,
        otherItems: List<RoleApplicationItem>,
    ): WearRoleApplicationPreference? =
        if (role.shouldShowNone()) {
            WearRoleApplicationPreference(
                    context = context,
                    defaultLabel = context.getString(R.string.default_app_none),
                    checked =
                        !(hasHolderApplication(recommendedItems) ||
                            hasHolderApplication(otherItems)),
                    onDefaultCheckChanged = { _ -> viewModel.setNoneDefaultApp() },
                )
                .apply { icon = context.getDrawable(R.drawable.ic_remove_circle) }
        } else {
            null
        }

    fun recommendedItemTitle(): String? =
        RoleUiBehaviorUtils.getRecommendedApplicationsTitle(role, context)

    fun recommendedItemDescription(): String? =
        RoleUiBehaviorUtils.getRecommendedApplicationsDescription(role, context)

    fun otherItemsTitle(): String? = context.resources.getString(R.string.default_app_others)

    fun getPreferences(
        applicationItems: List<RoleApplicationItem>
    ): List<WearRoleApplicationPreference> {
        return applicationItems
            .map { applicationItem ->
                val appInfo = applicationItem.applicationInfo
                val selected = applicationItem.isHolderApplication
                val user = UserHandle.getUserHandleForUid(appInfo.uid)
                WearRoleApplicationPreference(
                        context = context,
                        defaultLabel = Utils.getFullAppLabel(appInfo, context),
                        checked = selected,
                        onDefaultCheckChanged = { _ ->
                            run {
                                val packageName = appInfo.packageName
                                val confirmationDialogInfo =
                                    RoleUiBehaviorUtils.getConfirmationDialogInfo(
                                        role,
                                        packageName,
                                        user,
                                        context,
                                    )
                                if (confirmationDialogInfo != null) {
                                    showConfirmDialog(packageName, user, confirmationDialogInfo)
                                } else {
                                    setDefaultApp(packageName, user)
                                }
                            }
                        },
                    )
                    .apply {
                        icon = appInfo.loadIcon(context.packageManager)
                        setRestrictionIntent(
                            role.getApplicationRestrictionIntentAsUser(appInfo, user, context)
                        )
                        RoleUiBehaviorUtils.prepareApplicationPreferenceAsUser(
                            role,
                            this,
                            appInfo,
                            user,
                            context,
                        )
                    }
            }
            .toList()
    }

    private fun showConfirmDialog(
        packageName: String,
        userHandle: UserHandle,
        info: ConfirmationDialogInfo,
    ) {
        confirmDialogViewModel.confirmDialogArgs =
            ConfirmDialogArgs(
                info = info,
                onOkButtonClick = {
                    setDefaultApp(packageName, userHandle)
                    dismissConfirmDialog()
                },
                onCancelButtonClick = { dismissConfirmDialog() },
            )
        confirmDialogViewModel.showConfirmDialogLiveData.value = true
    }

    private fun dismissConfirmDialog() {
        confirmDialogViewModel.confirmDialogArgs = null
        confirmDialogViewModel.showConfirmDialogLiveData.value = false
    }

    private fun setDefaultApp(packageName: String, user: UserHandle) {
        viewModel.setDefaultApp(packageName, user)
    }

    fun getDescription() = context.getString(role.descriptionResource)

    private fun hasHolderApplication(applicationItems: List<RoleApplicationItem>): Boolean =
        applicationItems.map { it.isHolderApplication }.contains(true)
}
