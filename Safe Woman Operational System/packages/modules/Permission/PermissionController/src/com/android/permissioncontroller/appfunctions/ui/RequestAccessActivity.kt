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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil

class RequestAccessActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppFunctionsUtil.isAppFunctionUiEnabled(this)) {
            Log.w(
                LOG_TAG,
                "App Function isn't enabled: Either the platform is not supported " +
                    "or the UI flag FLAG_APP_FUNCTION_ACCESS_UI_ENABLED isn't enabled.",
            )
            setResultAndFinish(RESULT_CANCELED)
            return
        }

        val agentPackageName = getCallingPackage()
        val targetPackageName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        if (
            agentPackageName.isNullOrEmpty() ||
                targetPackageName.isNullOrEmpty() ||
                !AppFunctionsUtil.isValidAgent(agentPackageName, this) ||
                !AppFunctionsUtil.isValidTarget(targetPackageName, this)
        ) {
            Log.e(
                LOG_TAG,
                "Unknown/Invalid agent/target package. " +
                    "Target package: $targetPackageName. Agent package: $agentPackageName.",
            )
            setResultAndFinish(RESULT_CANCELED)
            return
        }

        // TODO: We should finish() the activity if the access is already granted.

        val fragment =
            RequestAppFunctionAccessFragment.newInstance(agentPackageName, targetPackageName)
        supportFragmentManager.beginTransaction().add(fragment, null).commit()
    }

    private fun setResultAndFinish(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    companion object {
        private val LOG_TAG = RequestAccessActivity::class.java.simpleName
    }
}
