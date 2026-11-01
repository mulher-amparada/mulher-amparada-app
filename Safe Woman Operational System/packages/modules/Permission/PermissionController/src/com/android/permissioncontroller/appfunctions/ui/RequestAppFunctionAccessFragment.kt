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

package com.android.permissioncontroller.appfunctions.ui

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.ui.viewmodel.RequestAppFunctionAccessViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.RequestAppFunctionAccessViewModelFactory
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RequestAppFunctionAccessFragment : DialogFragment() {
    private lateinit var agentPackageName: String
    private lateinit var targetPackageName: String

    private lateinit var viewModel: RequestAppFunctionAccessViewModel
    private lateinit var getAppFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase

    private lateinit var agentIconView: ImageView
    private lateinit var targetIconView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments!!
        agentPackageName = arguments.getString(AGENT_PACKAGE_NAME)!!
        targetPackageName = arguments.getString(TARGET_PACKAGE_NAME)!!
    }

    override fun onStart() {
        super.onStart()

        val factory =
            RequestAppFunctionAccessViewModelFactory(
                requireActivity().getApplication(),
                agentPackageName,
                targetPackageName,
            )

        viewModel =
            ViewModelProvider(this, factory).get(RequestAppFunctionAccessViewModel::class.java)

        val packageRepository = PackageRepository.createInstance(requireContext())
        getAppFunctionPackageInfoUseCase = GetAppFunctionPackageInfoUseCaseImpl(packageRepository)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(State.STARTED) {
                viewModel.uiStateFlow
                    .map { uiState ->
                        when (uiState) {
                            is Stateful.Failure -> Stateful.Failure(throwable = uiState.throwable)
                            is Stateful.Loading -> Stateful.Loading()
                            is Stateful.Success -> {
                                val agentPackageInfo =
                                    getAppFunctionPackageInfoUseCase(
                                        uiState.value.agentPackageName,
                                        requireContext(),
                                        Process.myUserHandle(),
                                    )!!
                                val targetPackageInfo =
                                    getAppFunctionPackageInfoUseCase(
                                        uiState.value.targetPackageName,
                                        requireContext(),
                                        Process.myUserHandle(),
                                    )!!
                                Stateful.Success(
                                    RequestAccessRichUiState(
                                        agentPackageInfo.label,
                                        targetPackageInfo.label,
                                        agentPackageInfo.icon,
                                        targetPackageInfo.icon,
                                        uiState.value.isRequestable,
                                    )
                                )
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect(::onUiStateChanged)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val view =
            LayoutInflater.from(activity)
                .inflate(R.layout.request_app_function_access_dialog, null)
                .apply {
                    agentIconView = requireViewById<ImageView>(R.id.agent_icon)
                    targetIconView = requireViewById<ImageView>(R.id.target_icon)
                    titleTextView = requireViewById<TextView>(R.id.title)
                    messageTextView = requireViewById<TextView>(R.id.description)
                    positiveButton = requireViewById<Button>(R.id.allow_button)
                    negativeButton = requireViewById<Button>(R.id.dont_allow_button)
                }
        positiveButton.apply { setOnClickListener { onAppFunctionGrant() } }
        negativeButton.apply { setOnClickListener { dialog!!.cancel() } }
        return Dialog(activity).apply { setContentView(view) }
    }

    private fun onUiStateChanged(uiState: Stateful<RequestAccessRichUiState>) {
        if (uiState is Stateful.Loading || uiState is Stateful.Failure) {
            // TODO: We should show a loading indicator in the dialog when in loading state
            return
        }

        val uiData = uiState.value!!
        val title =
            getString(
                R.string.request_app_function_access_dialog_title,
                uiData.agentLabel,
                uiData.targetLabel,
            )
        val message =
            getString(
                R.string.request_app_function_access_dialog_description,
                uiData.agentLabel,
                uiData.targetLabel,
            )

        agentIconView.setImageDrawable(uiData.agentIcon)
        targetIconView.setImageDrawable(uiData.targetIcon)
        titleTextView.text = title
        messageTextView.text = message
    }

    fun onAppFunctionGrant() {
        viewModel.grantAccess()
        setResultAndFinish(Activity.RESULT_OK)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setResultAndFinish(Activity.RESULT_CANCELED)
    }

    private fun setResultAndFinish(resultCode: Int) {
        val activity = requireActivity()
        activity.setResult(resultCode)
        activity.finish()
    }

    /** The data class for UI state of RequestAppFunctionAccess dialog. */
    data class RequestAccessRichUiState(
        val agentLabel: String,
        val targetLabel: String,
        val agentIcon: Drawable?,
        val targetIcon: Drawable?,
        val isRequestable: Boolean,
    )

    companion object {
        private const val AGENT_PACKAGE_NAME = "AGENT_PACKAGE_NAME"
        private const val TARGET_PACKAGE_NAME = "TARGET_PACKAGE_NAME"

        fun newInstance(
            agentPackageName: String,
            targetPackageName: String,
        ): RequestAppFunctionAccessFragment {
            val fragment =
                RequestAppFunctionAccessFragment().apply {
                    arguments =
                        Bundle().apply {
                            putString(AGENT_PACKAGE_NAME, agentPackageName)
                            putString(TARGET_PACKAGE_NAME, targetPackageName)
                        }
                }
            return fragment
        }
    }
}
