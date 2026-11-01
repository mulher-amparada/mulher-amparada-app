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

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.permissioncontroller.PermissionControllerStatsLog
import com.android.permissioncontroller.permission.utils.PackageRemovalMonitor
import com.android.permissioncontroller.role.UserPackage
import com.android.permissioncontroller.role.model.UserDeniedManager
import com.android.permissioncontroller.role.ui.ManageRoleHolderStateLiveData
import com.android.permissioncontroller.role.ui.RequestRoleViewModel
import com.android.permissioncontroller.role.ui.wear.model.WearRequestRoleViewModel
import com.android.permissioncontroller.role.ui.wear.model.WearRequestRoleViewModelFactory
import com.android.permissioncontroller.role.utils.PackageUtils
import com.android.permissioncontroller.role.utils.UserUtils
import com.android.role.controller.model.Role
import com.android.role.controller.model.Roles
import java.util.Objects

/** Wear specific version of [com.android.permissioncontroller.role.ui.RequestRoleFragment] */
class WearRequestRoleFragment : Fragment() {
    private lateinit var packageName: String
    private lateinit var roleName: String
    private lateinit var role: Role
    private lateinit var viewModel: RequestRoleViewModel
    private lateinit var wearViewModel: WearRequestRoleViewModel
    private lateinit var helper: WearRequestRoleHelper

    private var packageRemovalMonitor: PackageRemovalMonitor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        roleName = arguments?.getString(Intent.EXTRA_ROLE_NAME) ?: ""
        packageName = arguments?.getString(Intent.EXTRA_PACKAGE_NAME) ?: ""
        val context: Context = requireContext()

        role =
            Roles.get(context)[roleName]
                ?: let {
                    Log.e(TAG, "Unknown role: $roleName")
                    finish()
                    return null
                }
        val currentPackageNames =
            context.getSystemService(RoleManager::class.java)!!.getRoleHolders(roleName)
        if (currentPackageNames.contains(packageName)) {
            Log.i(
                TAG,
                "Application is already a role holder, role: $roleName, package: $packageName",
            )
            reportRequestResult(
                PermissionControllerStatsLog
                    .ROLE_REQUEST_RESULT_REPORTED__RESULT__IGNORED_ALREADY_GRANTED,
                null,
            )
            clearDeniedSetResultOkAndFinish()
            return null
        }
        val appInfo = PackageUtils.getApplicationInfo(packageName, context)
        if (appInfo == null) {
            Log.w(TAG, "Unknown application: $packageName")
            reportRequestResult(
                PermissionControllerStatsLog.ROLE_REQUEST_RESULT_REPORTED__RESULT__IGNORED,
                null,
            )
            finish()
            return null
        }

        viewModel =
            ViewModelProvider(
                    this,
                    RequestRoleViewModel.Factory(role, requireActivity().application),
                )
                .get(RequestRoleViewModel::class.java)
        viewModel.manageRoleHolderStateLiveData.observe(this, this::onManageRoleHolderStateChanged)

        wearViewModel =
            ViewModelProvider(this, WearRequestRoleViewModelFactory())
                .get(WearRequestRoleViewModel::class.java)

        savedInstanceState?.let { wearViewModel.onRestoreInstanceState(it) }

        helper =
            WearRequestRoleHelper(
                context,
                appInfo,
                role,
                roleName,
                packageName,
                viewModel,
                wearViewModel,
            )

        val onSetAsDefault: (Boolean, UserPackage?) -> Unit = { dontAskAgain, selectedUserPackage ->
            run {
                if (dontAskAgain) {
                    Log.i(
                        TAG,
                        "Request denied with don't ask again, role: $roleName" +
                            ", package: $packageName",
                    )
                    reportRequestResult(
                        PermissionControllerStatsLog
                            .ROLE_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED_WITH_ALWAYS,
                        null,
                    )
                    setDeniedAlwaysAndFinish()
                } else {
                    setRoleHolder(selectedUserPackage)
                }
            }
        }
        val onCanceled: () -> Unit = {
            Log.i(TAG, "Dialog cancelled, role: $roleName , package: $packageName")
            reportRequestResult(
                PermissionControllerStatsLog.ROLE_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED,
                null,
            )
            setDeniedOnceAndFinish()
        }

        return ComposeView(context).apply {
            setContent { WearRequestRoleScreen(helper, onSetAsDefault, onCanceled) }
        }
    }

    private fun onManageRoleHolderStateChanged(state: Int) {
        val liveData = viewModel.manageRoleHolderStateLiveData
        when (state) {
            ManageRoleHolderStateLiveData.STATE_SUCCESS -> {
                val lastPackageName = liveData.lastPackageName
                val lastUser = liveData.lastUser
                val lastUserPackage: UserPackage? =
                    if (lastPackageName == null || lastUser == null) {
                        null
                    } else {
                        UserPackage.of(lastUser, lastPackageName)
                    }
                if (lastPackageName != null) {
                    role.onHolderSelectedAsUser(lastPackageName, lastUser, requireContext())
                }
                if (isRequestingApplication(lastUserPackage)) {
                    Log.i(
                        TAG,
                        "Application added as a role holder, role: $roleName, package: " +
                            packageName,
                    )
                    clearDeniedSetResultOkAndFinish()
                } else {
                    Log.i(
                        TAG,
                        "Request denied with another application added as a role holder, " +
                            "role: $roleName, package: $packageName",
                    )
                    setDeniedOnceAndFinish()
                }
            }
            ManageRoleHolderStateLiveData.STATE_FAILURE -> finish()
        }
    }

    private fun clearDeniedSetResultOkAndFinish() {
        UserDeniedManager.getInstance(requireContext()).clearDenied(roleName, packageName)
        requireActivity().setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setDeniedOnceAndFinish() {
        UserDeniedManager.getInstance(requireContext()).setDeniedOnce(roleName, packageName)
        finish()
    }

    private fun reportRequestResult(result: Int, grantedUserPackage: UserPackage?) {
        val holderUserPackage: UserPackage? = getHolderUserPackage()
        reportRequestResult(
            getRequestingApplicationUid(),
            packageName,
            roleName,
            getQualifyingApplicationCount(),
            getQualifyingApplicationUid(holderUserPackage),
            holderUserPackage?.packageName,
            getQualifyingApplicationUid(grantedUserPackage),
            grantedUserPackage?.packageName,
            result,
        )
    }

    private fun getRequestingApplicationUid(): Int {
        val uid: Int =
            getQualifyingApplicationUid(UserPackage.of(Process.myUserHandle(), packageName))
        if (uid != -1) {
            return uid
        }
        val applicationInfo =
            PackageUtils.getApplicationInfo(packageName, requireActivity()) ?: return -1
        return applicationInfo.uid
    }

    private fun getQualifyingApplicationUid(userPackage: UserPackage?): Int {
        if (userPackage == null) {
            return -1
        }
        viewModel.liveData.value?.let { applicationItems ->
            for (applicationItem in applicationItems) {
                val qualifyingApplicationInfo = applicationItem.applicationInfo
                val qualifyingUserPackage = UserPackage.from(qualifyingApplicationInfo)
                if (Objects.equals(qualifyingUserPackage, userPackage)) {
                    return qualifyingApplicationInfo.uid
                }
            }
        }
        return -1
    }

    private fun getHolderUserPackage(): UserPackage? {
        viewModel.liveData.value?.let { qualifyingApplications ->
            for (qualifyingApplication in qualifyingApplications) {
                if (qualifyingApplication.isHolderApplication) {
                    return UserPackage.from(qualifyingApplication.applicationInfo)
                }
            }
        }
        return null
    }

    private fun getQualifyingApplicationCount(): Int {
        return viewModel.liveData.value?.size ?: -1
    }

    private fun setDeniedAlwaysAndFinish() {
        UserDeniedManager.getInstance(requireContext()).setDeniedAlways(roleName, packageName)
        finish()
    }

    private fun finish() {
        requireActivity().finish()
    }

    private fun setRoleHolder(selectedUserPackage: UserPackage?) {
        val context: Context = requireContext()
        if (selectedUserPackage == null) {
            reportRequestResult(
                PermissionControllerStatsLog
                    .ROLE_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED_GRANTED_ANOTHER,
                null,
            )
            val user: UserHandle =
                if (role.exclusivity == Role.EXCLUSIVITY_PROFILE_GROUP) {
                    UserUtils.getProfileParentOrSelf(Process.myUserHandle(), context)
                } else {
                    Process.myUserHandle()
                }
            role.onNoneHolderSelectedAsUser(user, context)
            viewModel.manageRoleHolderStateLiveData.clearRoleHoldersAsUser(
                roleName,
                0,
                user,
                context,
            )
        } else {
            val isRequestingApplication = isRequestingApplication(selectedUserPackage)
            if (isRequestingApplication) {
                reportRequestResult(
                    PermissionControllerStatsLog.ROLE_REQUEST_RESULT_REPORTED__RESULT__USER_GRANTED,
                    null,
                )
            } else {
                reportRequestResult(
                    PermissionControllerStatsLog
                        .ROLE_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED_GRANTED_ANOTHER,
                    selectedUserPackage,
                )
            }
            val flags =
                if (isRequestingApplication) RoleManager.MANAGE_HOLDERS_FLAG_DONT_KILL_APP else 0
            viewModel.manageRoleHolderStateLiveData.setRoleHolderAsUser(
                roleName,
                selectedUserPackage.packageName,
                true,
                flags,
                selectedUserPackage.user,
                context,
            )
        }
    }

    private fun isRequestingApplication(userPackage: UserPackage?): Boolean {
        return Objects.equals(userPackage, UserPackage.of(Process.myUserHandle(), packageName))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wearViewModel.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        packageRemovalMonitor =
            object : PackageRemovalMonitor(requireContext(), packageName) {
                    override fun onPackageRemoved() {
                        Log.w(
                            TAG,
                            "Application is uninstalled, role: $roleName" +
                                ", package: " +
                                packageName,
                        )
                        reportRequestResult(
                            PermissionControllerStatsLog
                                .ROLE_REQUEST_RESULT_REPORTED__RESULT__IGNORED,
                            null,
                        )
                        finish()
                    }
                }
                .apply { register() }
    }

    override fun onStop() {
        super.onStop()
        packageRemovalMonitor?.let {
            it.unregister()
            packageRemovalMonitor = null
        }
    }

    companion object {
        const val TAG = "WearRequestRoleFragment"

        /** Creates a new instance of [WearRequestRoleFragment]. */
        @JvmStatic
        fun newInstance(roleName: String, packageName: String): WearRequestRoleFragment {
            return WearRequestRoleFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(Intent.EXTRA_ROLE_NAME, roleName)
                        putString(Intent.EXTRA_PACKAGE_NAME, packageName)
                    }
            }
        }

        @JvmStatic
        fun reportRequestResult(
            requestingUid: Int,
            requestingPackageName: String,
            roleName: String,
            qualifyingCount: Int,
            currentUid: Int,
            currentPackageName: String?,
            grantedAnotherUid: Int,
            grantedAnotherPackageName: String?,
            result: Int,
        ) {
            Log.i(
                TAG,
                "Role request result requestingUid=$requestingUid" +
                    " requestingPackageName=$requestingPackageName" +
                    " roleName=$roleName" +
                    " qualifyingCount=$qualifyingCount" +
                    " currentUid=$currentUid" +
                    " currentPackageName=$currentPackageName" +
                    " grantedAnotherUid=$grantedAnotherUid" +
                    " grantedAnotherPackageName=$grantedAnotherPackageName" +
                    " result=$result",
            )
            PermissionControllerStatsLog.write(
                PermissionControllerStatsLog.ROLE_REQUEST_RESULT_REPORTED,
                requestingUid,
                requestingPackageName,
                roleName,
                qualifyingCount,
                currentUid,
                currentPackageName,
                grantedAnotherUid,
                grantedAnotherPackageName,
                result,
            )
        }
    }
}
