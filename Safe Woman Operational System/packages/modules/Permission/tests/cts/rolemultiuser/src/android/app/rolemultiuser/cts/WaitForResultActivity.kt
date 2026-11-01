/*
 * Copyright (C) 2024 The Android Open Source Project
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
package android.app.rolemultiuser.cts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** An Activity that can start another Activity and wait for its result. */
class WaitForResultActivity : Activity() {
    private var mLatch: CountDownLatch? = null
    private var mResultCode = 0
    private var mData: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            throw RuntimeException(
                ("Activity was recreated (perhaps due to a configuration change?) " +
                    "and this activity doesn't currently know how to gracefully handle " +
                    "configuration changes.")
            )
        }
    }

    fun startActivityToWaitForResult(intent: Intent) {
        mLatch = CountDownLatch(1)
        startActivityForResult(intent, REQUEST_CODE_WAIT_FOR_RESULT)
    }

    @Throws(InterruptedException::class)
    fun waitForActivityResult(timeoutMillis: Long): Pair<Int, Intent?> {
        mLatch!!.await(timeoutMillis, TimeUnit.MILLISECONDS)
        return Pair(mResultCode, mData)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_WAIT_FOR_RESULT) {
            mResultCode = resultCode
            mData = data
            mLatch!!.countDown()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val REQUEST_CODE_WAIT_FOR_RESULT = 1
    }
}
