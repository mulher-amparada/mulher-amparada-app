/**
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *      http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.tests.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.data.entries.EntriesViewModel
import dagger.hilt.android.AndroidEntryPoint

/** A fragment with an empty layout that can be used to host the [AllEntriesFragment] in tests */
@AndroidEntryPoint(Fragment::class)
class FakeParentFragment : Hilt_FakeParentFragment() {
    // Needed for the AllEntriesFragment viewModel lifecycle
    private val entriesViewModel: EntriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // This layout is currently empty
        return inflater.inflate(R.layout.fragment_empty, container, false)
    }
}
