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

package com.android.permissioncontroller.role.ui.v37

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.android.permissioncontroller.PermissionControllerStatsLog
import com.android.permissioncontroller.PermissionControllerStatsLog.REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED
import com.android.permissioncontroller.PermissionControllerStatsLog.REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_CANCELED
import com.android.permissioncontroller.PermissionControllerStatsLog.REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_USER_DENIED
import com.android.permissioncontroller.PermissionControllerStatsLog.REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_USER_GRANTED
import com.android.permissioncontroller.R
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository

class RequestReadScreenContextFragment : DialogFragment() {
    private lateinit var packageName: String

    private lateinit var viewModel: RequestReadScreenContextViewModel

    private lateinit var iconImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments!!
        packageName = arguments.getString(Intent.EXTRA_PACKAGE_NAME)!!
    }

    override fun onStart() {
        super.onStart()

        val factory =
            RequestReadScreenContextViewModelFactory(
                requireActivity().getApplication(),
                packageName,
            )

        viewModel =
            ViewModelProvider(this, factory).get(RequestReadScreenContextViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val view =
            LayoutInflater.from(activity).inflate(R.layout.request_read_screen_context_dialog, null)
        iconImage = view.requireViewById(R.id.icon)
        titleText = view.requireViewById(R.id.title)
        positiveButton = view.requireViewById(R.id.allow_button)
        negativeButton = view.requireViewById(R.id.dont_allow_button)

        val packageRepository = PackageRepository.createInstance(requireContext())
        val icon = packageRepository.getBadgedPackageIcon(packageName, Process.myUserHandle())
        val label = packageRepository.getPackageLabel(packageName, Process.myUserHandle())
        val escapedAppLabel = Html.escapeHtml(label)
        val title =
            Html.fromHtml(
                getString(R.string.request_read_screen_context_dialog_title, escapedAppLabel),
                0,
            )

        iconImage.setImageDrawable(icon)
        titleText.text = title

        positiveButton.apply { setOnClickListener { onGrant() } }
        negativeButton.apply { setOnClickListener { onDeny() } }
        return Dialog(activity).apply {
            setContentView(view)
            setCanceledOnTouchOutside(false)
        }
    }

    // onGrant explicitly calls dismiss on dialog before finishing. onCancel is not called if
    // dismissed explicitly
    fun onGrant() {
        viewModel.setAllowed()
        reportRequestResult(
            requireContext(),
            packageName,
            REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_USER_GRANTED,
        )
        // onGrant explicitly calls dismiss on dialog before finishing. onCancel is not called if
        // dismissed explicitly
        dismiss()
        setResultAndFinish(Activity.RESULT_OK)
    }

    // onDeny explicitly calls dismiss on dialog before finishing. onCancel is not called if
    // dismissed explicitly.
    fun onDeny() {
        viewModel.markRequestDenied()
        reportRequestResult(
            requireContext(),
            packageName,
            REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_USER_DENIED,
        )
        // onDeny explicitly calls dismiss on dialog before finishing. onCancel is not called if
        // dismissed explicitly.
        dismiss()
        setResultAndFinish(Activity.RESULT_CANCELED)
    }

    // Dialog's onCancel is called before onDismiss when dialog is cancelled via back,
    // touch outside, etc. But not when dismiss is called explicitly. Dismiss will be called by
    // dialog internals after this method.
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        reportRequestResult(
            requireContext(),
            packageName,
            REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED__RESULT__RESULT_CANCELED,
        )
        setResultAndFinish(Activity.RESULT_CANCELED)
    }

    private fun setResultAndFinish(resultCode: Int) {
        val activity = requireActivity()
        activity.setResult(resultCode)
        activity.finish()
    }

    companion object {
        private const val DEBUG = false
        private val LOG_TAG = RequestReadScreenContextFragment::class.java.simpleName

        fun newInstance(packageName: String): RequestReadScreenContextFragment =
            RequestReadScreenContextFragment().apply {
                arguments = Bundle().apply { putString(Intent.EXTRA_PACKAGE_NAME, packageName) }
            }

        internal fun reportRequestResult(context: Context, packageName: String?, result: Int) {
            val uid =
                packageName?.let {
                    PackageRepository.createInstance(context)
                        .getPackageUid(packageName, Process.myUserHandle())
                } ?: Process.INVALID_UID
            PermissionControllerStatsLog.write(
                REQUEST_READ_SCREEN_CONTEXT_DIALOG_ACTION_REPORTED,
                uid,
                packageName ?: "",
                result,
            )
            if (DEBUG) {
                Log.d(
                    LOG_TAG,
                    "Read screen context request - uid:$uid, packageName:$packageName, " +
                        "result:$result",
                )
            }
        }
    }
}
