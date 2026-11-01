/*
 * Copyright (C) 2026 The Android Open Source Project
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

package android.permissionui.cts.appthataccesseslocation

import android.app.Activity
import android.app.AppOpsManager
import android.os.Bundle
import android.os.Process
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val USE_LOCATION = "use_location"
private const val FINISH_EARLY = "finish_early"
private const val USE_DURATION_MS = 10000L
private const val SAMPLE_RATE_HZ = 44100

/** Activity which will use location. */
class AccessLocationActivity : Activity() {
    private var appOpsManager: AppOpsManager? = null
    private var useLocation = false
    private var locationFinished = false
    private var runLocation = false
    private var finishEarly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            throw RuntimeException(
                "Activity was recreated (perhaps due to a configuration change?) " +
                    "and this activity doesn't currently know how to gracefully handle " +
                    "configuration changes."
            )
        }
    }

    override fun onStart() {
        super.onStart()
        runLocation = intent.getBooleanExtra(USE_LOCATION, false)
        finishEarly = intent.getBooleanExtra(FINISH_EARLY, false)

        if (runLocation) {
            useLocation()
        }
    }

    override fun finish() {
        super.finish()
        if (runLocation) {
            appOpsManager?.finishOp(AppOpsManager.OPSTR_FINE_LOCATION, Process.myUid(), packageName)
        }
        appOpsManager = null
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun useLocation() {
        appOpsManager = getSystemService(AppOpsManager::class.java)
        appOpsManager?.startOpNoThrow(
            AppOpsManager.OPSTR_FINE_LOCATION,
            Process.myUid(),
            packageName,
        )
        if (finishEarly) {
            appOpsManager?.finishOp(AppOpsManager.OPSTR_FINE_LOCATION, Process.myUid(), packageName)
            return
        }
        GlobalScope.launch {
            delay(USE_DURATION_MS)
            locationFinished = true
            finishIfAllDone()
        }
    }

    private fun finishIfAllDone() {
        if ((!runLocation || locationFinished)) {
            finish()
        }
    }
}
