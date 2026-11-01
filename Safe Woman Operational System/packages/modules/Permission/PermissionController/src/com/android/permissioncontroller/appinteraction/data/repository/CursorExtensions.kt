/*
 * Copyright 2025 The Android Open Source Project
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

package com.android.permissioncontroller.appinteraction.data.repository

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

fun Cursor.requireStringOrThrow(columnName: String): String =
    getString(getColumnIndexOrThrow(columnName))

fun Cursor.getStringOrThrow(columnName: String): String? =
    getStringOrNull(getColumnIndexOrThrow(columnName))

fun Cursor.requireLongOrThrow(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))

fun Cursor.getIntOrThrow(columnName: String): Int? = getIntOrNull(getColumnIndexOrThrow(columnName))
