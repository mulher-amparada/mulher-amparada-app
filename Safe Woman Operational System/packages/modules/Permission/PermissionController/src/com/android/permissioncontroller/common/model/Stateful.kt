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

package com.android.permissioncontroller.common.model

/**
 * A stateful object that can be in one of three states: loading, success, or failure.
 *
 * @param T The type of the value.
 */
sealed class Stateful<T> {
    abstract val value: T?

    /**
     * Loading state for the stateful object.
     *
     * @param value The value of the stateful object.
     */
    data class Loading<T>(override val value: T? = null) : Stateful<T>()

    /**
     * Success state for the stateful object.
     *
     * @param value The value of the stateful object.
     */
    data class Success<T>(override val value: T) : Stateful<T>()

    /**
     * Failure state for the stateful object.
     *
     * Note that value parameter is optional and is intended for showing something (e.g. previous
     * data) when there's an error loading new data. Feel free to ignore it if you don't have such a
     * case for your UI.
     *
     * @param value The value of the stateful object.
     * @param throwable The throwable that caused the failure.
     */
    data class Failure<T>(override val value: T? = null, val throwable: Throwable) : Stateful<T>()
}
