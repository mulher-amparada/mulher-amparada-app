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
package com.android.permissioncontroller.wear.permission.components.theme

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DoNotInline
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.android.permission.flags.Flags

object ResourceHelper {

    val materialUIVersionInApp: WearPermissionMaterialUIVersion =
        WearPermissionMaterialUIVersion.MATERIAL3

    private val material3EnabledInSettings: Boolean
        get() {
            return Flags.wearComposeMaterial3()
        }

    val materialUIVersionInSettings: WearPermissionMaterialUIVersion =
        if (material3EnabledInSettings) {
            WearPermissionMaterialUIVersion.MATERIAL3
        } else {
            WearPermissionMaterialUIVersion.MATERIAL2_5
        }

    @DoNotInline
    fun getColor(context: Context, @ColorRes id: Int): Color? {
        return try {
            val colorInt = context.resources.getColor(id, context.theme)
            Color(colorInt)
        } catch (_: Exception) {
            null
        }
    }

    @DoNotInline
    fun getString(context: Context, @StringRes id: Int): String? {
        return try {
            context.resources.getString(id)
        } catch (_: Exception) {
            null
        }
    }

    @DoNotInline
    fun getDimen(context: Context, @DimenRes id: Int): Float? {
        return try {
            context.resources.getDimension(id) / context.resources.displayMetrics.density
        } catch (_: Exception) {
            null
        }
    }
}
