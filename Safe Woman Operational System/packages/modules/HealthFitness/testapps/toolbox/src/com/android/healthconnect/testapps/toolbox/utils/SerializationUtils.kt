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
package com.android.healthconnect.testapps.toolbox.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

internal fun Serializable.serialize(): ByteArray =
    ByteArrayOutputStream().use { output ->
        ObjectOutputStream(output).use { it.writeObject(this) }
        output.toByteArray()
    }

internal fun <T : Serializable> ByteArray.deserialize(): T =
    ByteArrayInputStream(this).use { input ->
        ObjectInputStream(input).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as T
        }
    }
