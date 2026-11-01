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
package com.android.healthconnect.testapps.toolbox.utils

import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.DirectExecutor
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Launches a new coroutine and wraps it in [ListenableFuture].
 *
 * Temporary until the stable version 1.2.0 of androidx.concurrent:concurrent-futures-ktx,
 * where SuspendToFutureAdapter will be available.
 */
internal fun <T> launchFuture(
    context: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> T,
): ListenableFuture<T> =
    CallbackToFutureAdapter.getFuture { completer ->
        val scope = CoroutineScope(context)

        scope.launch {
            try {
                completer.set(block())
            } catch (e: CancellationException) {
                completer.setCancelled()
            } catch (e: Throwable) {
                completer.setException(e)
            }
        }

        completer.addCancellationListener(Runnable(scope::cancel), DirectExecutor.INSTANCE)
    }
