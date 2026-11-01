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

package com.android.permissioncontroller.safetycenter.ui.expressive

import android.content.Context
import android.os.Build
import android.safetycenter.SafetyCenterIssue
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.R
import com.android.permissioncontroller.safetycenter.ui.Action
import com.android.permissioncontroller.safetycenter.ui.ComparablePreference
import com.android.permissioncontroller.safetycenter.ui.IssueCardPreference.ConfirmActionDialogFragment
import com.android.permissioncontroller.safetycenter.ui.IssueCardPreference.ConfirmDismissalDialogFragment
import com.android.permissioncontroller.safetycenter.ui.model.IssueUiData
import com.android.permissioncontroller.safetycenter.ui.model.SafetyCenterViewModel
import com.android.settingslib.widget.BannerMessagePreference

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class SafetyBannerMessagePreference(
    context: Context,
    private val issueUiData: IssueUiData,
    private val viewModel: SafetyCenterViewModel,
    private val dialogFragmentManager: FragmentManager,
) : BannerMessagePreference(context), ComparablePreference {

    init {
        setButtonOrientation(LinearLayout.VERTICAL)
        displayIssue()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        viewModel.interactionLogger.recordIssueViewed(issueUiData.issue, issueUiData.isDismissed)
    }

    private fun displayIssue() {
        setAttentionLevel(issueUiData.issue.severityLevel.toAttentionLevel())

        title = issueUiData.issue.title
        summary = issueUiData.issue.summary
        setHeader(issueUiData.issue.attributionTitle)
        setSubtitle(issueUiData.issue.subtitle)
        // Note: BannerMessagePreference i think always shows an icon (even if it's set to null),
        // which is not in the spec

        configureDismissButton()
        configureActionButtons()
        maybeStartResolution()
    }

    private fun configureDismissButton() {
        if (issueUiData.issue.isDismissible && !issueUiData.isDismissed) {
            setDismissButtonVisible(true)
            setDismissButtonOnClickListener {
                if (issueUiData.issue.shouldConfirmDismissal()) {
                    ConfirmDismissalDialogFragment.newInstance(issueUiData.issue)
                        .showNow(dialogFragmentManager, /* tag= */ null)
                } else {
                    viewModel.dismissIssue(issueUiData.issue)
                    viewModel.interactionLogger.recordForIssue(
                        Action.ISSUE_DISMISS_CLICKED,
                        issueUiData.issue,
                        isDismissed = false,
                    )
                }
            }
        } else {
            setDismissButtonVisible(false)
            setDismissButtonOnClickListener(null)
        }
    }

    private fun configureActionButtons() {
        val primaryAction = issueUiData.issue.actions.getOrNull(0)
        if (primaryAction != null) {
            setPositiveButtonText(primaryAction.label)
            setPositiveButtonEnabled(issueUiData.resolvedIssueActionId != primaryAction.id)
            setPositiveButtonVisible(true)
            setPositiveButtonOnClickListener(
                ActionButtonOnClickListener(primaryAction, isPrimaryButton = true)
            )
        } else {
            setPositiveButtonVisible(false)
            setPositiveButtonOnClickListener(null)
        }

        val secondaryAction = issueUiData.issue.actions.getOrNull(1)
        if (secondaryAction != null) {
            setNegativeButtonText(secondaryAction.label)
            setNegativeButtonEnabled(issueUiData.resolvedIssueActionId != secondaryAction.id)
            setNegativeButtonVisible(true)
            setNegativeButtonOnClickListener(
                ActionButtonOnClickListener(secondaryAction, isPrimaryButton = false)
            )
        } else {
            setNegativeButtonVisible(false)
            setNegativeButtonOnClickListener(null)
        }
    }

    private inner class ActionButtonOnClickListener(
        private val action: SafetyCenterIssue.Action,
        private val isPrimaryButton: Boolean,
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            if (SdkLevel.isAtLeastU() && action.confirmationDialogDetails != null) {
                ConfirmActionDialogFragment.newInstance(
                        issueUiData.issue,
                        action,
                        issueUiData.launchTaskId,
                        isPrimaryButton,
                        issueUiData.isDismissed,
                    )
                    .showNow(dialogFragmentManager, /* tag= */ null)
            } else {
                if (action.willResolve()) {
                    setPositiveButtonEnabled(false)
                }
                viewModel.executeIssueAction(issueUiData.issue, action, issueUiData.launchTaskId)
                viewModel.interactionLogger.recordForIssue(
                    if (isPrimaryButton) {
                        Action.ISSUE_PRIMARY_ACTION_CLICKED
                    } else {
                        Action.ISSUE_SECONDARY_ACTION_CLICKED
                    },
                    issueUiData.issue,
                    issueUiData.isDismissed,
                )
            }
        }
    }

    private fun maybeStartResolution() {
        val resolvedActionId = issueUiData.resolvedIssueActionId ?: return

        val action = issueUiData.issue.actions.firstOrNull { it.id == resolvedActionId } ?: return
        val successMessage =
            action.successMessage?.ifEmpty { null }
                ?: context.getString(R.string.safety_center_resolved_issue_fallback)

        showResolutionAnimation(successMessage) {
            viewModel.markIssueResolvedUiCompleted(issueUiData.issue.id)
        }
    }

    private fun Int.toAttentionLevel(): AttentionLevel {
        return when (this) {
            SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_OK -> AttentionLevel.LOW
            SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_RECOMMENDATION -> AttentionLevel.MEDIUM
            SafetyCenterIssue.ISSUE_SEVERITY_LEVEL_CRITICAL_WARNING -> AttentionLevel.HIGH
            else -> {
                Log.w(TAG, "Unexpected issue severity level $this")
                AttentionLevel.LOW
            }
        }
    }

    private companion object {
        const val TAG = "SafetyBannerMessagePref"
    }

    override fun isSameItem(preference: Preference): Boolean =
        preference is SafetyBannerMessagePreference &&
            preference.issueUiData.issue.id == issueUiData.issue.id

    override fun hasSameContents(preference: Preference): Boolean =
        preference is SafetyBannerMessagePreference && preference.issueUiData == issueUiData
}
