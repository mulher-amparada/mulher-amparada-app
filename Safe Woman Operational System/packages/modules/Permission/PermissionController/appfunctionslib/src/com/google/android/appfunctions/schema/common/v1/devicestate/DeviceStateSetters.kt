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

package com.google.android.appfunctions.schema.common.v1.devicestate

import androidx.appsearch.annotation.Document
import java.util.Objects

/** Sets device states. */
@AppFunctionSchemaDefinition(
    name = "setDeviceStateItem",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface SetDeviceStateItem {
    /**
     * Sets the device state using the provided params.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param setDeviceStateItemParams the request containing the required params to set the device
     *   state
     */
    suspend fun setDeviceStateItem(
        appFunctionContext: AppFunctionContext,
        setDeviceStateItemParams: SetDeviceStateItemParams,
    ): SetDeviceStateItemResponse
}

/** Adjusts device states. */
@AppFunctionSchemaDefinition(
    name = "adjustNumericDeviceStateItemByPercentage",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface AdjustNumericDeviceStateItemByPercentage {
    /**
     * Adjusts the device state that is of a numeric type (e.g. brightness, ringtone, etc) by a
     * percentage using the provided params.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param adjustNumericDeviceStateItemByPercentageParams the request containing the required
     *   params to adjust the device state
     */
    suspend fun adjustNumericDeviceStateItemByPercentage(
        appFunctionContext: AppFunctionContext,
        adjustNumericDeviceStateItemByPercentageParams:
            AdjustNumericDeviceStateItemByPercentageParams,
    ): SetDeviceStateItemResponse
}

/** Adjusts device states. */
@AppFunctionSchemaDefinition(
    name = "offsetNumericDeviceStateItemByValue",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface OffsetNumericDeviceStateItemByValue {
    /**
     * Adjusts the device state that is of a numeric type (e.g. brightness, ringtone, etc) using the
     * provided params.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param offsetNumericDeviceStateItemByValueParams the request containing the required params
     *   to adjust the device state
     */
    suspend fun offsetNumericDeviceStateItemByValue(
        appFunctionContext: AppFunctionContext,
        offsetNumericDeviceStateItemByValueParams: OffsetNumericDeviceStateItemByValueParams,
    ): SetDeviceStateItemResponse
}

/** Represents the request that is passed in to set a certain device state. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.SetDeviceStateItemParams"
)
class SetDeviceStateItemParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The unique identifier of the setting to change. */
    @Document.StringProperty(required = true) val key: String,
    /** A list of itemization keys required to identify an itemized preference . */
    @Document.StringProperty val itemizationKeys: List<String> = emptyList(),
    /**
     * The new value to set. The caller is responsible for providing the appropriate value type in a
     * String representation (e.g. "True", "0", or a json representation of a custom object).
     */
    @Document.StringProperty(required = true) val value: String,
    /**
     * Indicates that the request was initiated by the user while the device was unlocked and so the
     * app function should skip the device lock state check before executing the function. This
     * should only be used by the calling agent for scheduled requests and for handling ongoing
     * requests that were started before the device was locked. Defaults to checking for the lock
     * state if not provided.
     */
    @Document.BooleanProperty val requestInitiatedWhileUnlocked: Boolean? = null,
) {
    override fun equals(other: Any?) =
        other is SetDeviceStateItemParams &&
            key == other.key &&
            itemizationKeys == other.itemizationKeys &&
            value == other.value &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() =
        Objects.hash(key, itemizationKeys, value, requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to adjust a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.OffsetNumericDeviceStateItemByValueParams"
)
class OffsetNumericDeviceStateItemByValueParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The unique identifier of the setting to change. */
    @Document.StringProperty(required = true) val key: String,
    /** A list of itemization keys required to identify an itemized preference . */
    @Document.StringProperty val itemizationKeys: List<String> = emptyList(),
    /**
     * A value adjustment to be applied to the current value. e.g. decrease ringtone by 2 levels.
     */
    @Document.DoubleProperty(required = true) val valueAdjustment: Double,
    /**
     * Indicates that the request was initiated by the user while the device was unlocked and so the
     * app function should skip the device lock state check before executing the function. This
     * should only be used by the calling agent for scheduled requests and for handling ongoing
     * requests that were started before the device was locked. Defaults to checking for the lock
     * state if not provided.
     */
    @Document.BooleanProperty val requestInitiatedWhileUnlocked: Boolean? = null,
) {
    override fun equals(other: Any?) =
        other is OffsetNumericDeviceStateItemByValueParams &&
            key == other.key &&
            itemizationKeys == other.itemizationKeys &&
            valueAdjustment == other.valueAdjustment &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() =
        Objects.hash(key, itemizationKeys, valueAdjustment, requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to adjust a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.AdjustNumericDeviceStateItemByPercentageParams"
)
class AdjustNumericDeviceStateItemByPercentageParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The unique identifier of the setting to change. */
    @Document.StringProperty(required = true) val key: String,
    /** A list of itemization keys required to identify an itemized preference . */
    @Document.StringProperty val itemizationKeys: List<String> = emptyList(),
    /**
     * An adjustment percentage to be applied to the current value, e.g. "reduce brightness by 50%".
     * Note that "100%" is a no change.
     */
    @Document.LongProperty(required = true) val percentageAdjustment: Int,
    /**
     * Indicates that the request was initiated by the user while the device was unlocked and so the
     * app function should skip the device lock state check before executing the function. This
     * should only be used by the calling agent for scheduled requests and for handling ongoing
     * requests that were started before the device was locked. Defaults to checking for the lock
     * state if not provided.
     */
    @Document.BooleanProperty val requestInitiatedWhileUnlocked: Boolean? = null,
) {
    override fun equals(other: Any?) =
        other is AdjustNumericDeviceStateItemByPercentageParams &&
            key == other.key &&
            itemizationKeys == other.itemizationKeys &&
            percentageAdjustment == other.percentageAdjustment &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() =
        Objects.hash(key, itemizationKeys, percentageAdjustment, requestInitiatedWhileUnlocked)
}

/**
 * Represents the metadata of relevant device settings, structured for consumption by an LLM. This
 * serves as the top-level response object when querying device state schema.
 */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.SetDeviceStateItemResponse"
)
class SetDeviceStateItemResponse(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** Whether the setting has been successfully set. */
    @Document.BooleanProperty(required = true) val isSuccessful: Boolean,
    /** The current value after the request has been executed. */
    @Document.StringProperty(required = true) val currentValue: String,
    /**
     * The reason it failed to execute e.g. "DISABLED", "RESTRICTED", "UNAVAILABLE", "REQUIRES
     * PERMISSION".
     */
    @Document.StringProperty val failureReason: String? = null,
) {
    override fun equals(other: Any?) =
        other is SetDeviceStateItemResponse &&
            isSuccessful == other.isSuccessful &&
            currentValue == other.currentValue &&
            failureReason == other.failureReason

    override fun hashCode() = Objects.hash(isSuccessful, currentValue, failureReason)
}
