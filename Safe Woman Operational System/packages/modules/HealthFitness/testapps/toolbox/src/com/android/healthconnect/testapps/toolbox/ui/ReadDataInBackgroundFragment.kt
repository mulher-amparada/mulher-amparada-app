/**
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *    http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.testapps.toolbox.ui

import android.health.connect.datatypes.StepsRecord
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.healthconnect.testapps.toolbox.PKG_TOOLBOX_2
import com.android.healthconnect.testapps.toolbox.R
import com.android.healthconnect.testapps.toolbox.ToolboxProxyPayload
import com.android.healthconnect.testapps.toolbox.callToolbox
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.showMessageDialog
import kotlinx.coroutines.launch

class ReadDataInBackgroundFragment : Fragment(R.layout.fragment_read_data_in_background) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.requireViewById<Button>(R.id.read_own_steps_button).setOnClickListener {
            callToolbox2(
                ToolboxProxyPayload.ReadRecords(
                    type = StepsRecord::class.java,
                    packageNames = listOf(PKG_TOOLBOX_2),
                )
            )
        }

        view.requireViewById<Button>(R.id.read_all_steps_button).setOnClickListener {
            callToolbox2(ToolboxProxyPayload.ReadRecords(type = StepsRecord::class.java))
        }
    }

    private fun callToolbox2(payload: ToolboxProxyPayload) {
        lifecycleScope.launch {
            val response =
                callToolbox(
                    context = requireContext(),
                    packageName = PKG_TOOLBOX_2,
                    payload = payload,
                )

            requireContext().showMessageDialog(response ?: "No response")
        }
    }
}
