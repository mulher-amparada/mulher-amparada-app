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

import android.content.Context
import androidx.appsearch.annotation.Document
import java.util.Objects

public const val DEVICE_STATE_CATEGORY = "device_state"

/** The execution context of app function. */
public interface AppFunctionContext {
    /** The Android context. */
    public val context: Context

    /**
     * Return the name of the package that invoked this AppFunction. You can use this information to
     * validate the caller.
     */
    public val callingPackageName: String
}

/** Annotates an interface that defines the app function schema interface. */
// Binary because it's used to determine the schema name and version from the
// compiled schema library.
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class AppFunctionSchemaDefinition(
    val name: String,
    val version: Int,
    val category: String,
)

/** Gets uncategorized device states. */
@AppFunctionSchemaDefinition(
    name = "getUncategorizedDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetUncategorizedDeviceState {
    /**
     * Gets uncategorized device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getUncategorizedDeviceStateParams the request containing the required params to get
     *   the device state item.
     */
    suspend fun getUncategorizedDeviceState(
        appFunctionContext: AppFunctionContext,
        getUncategorizedDeviceStateParams: GetUncategorizedDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets a particular device state item. */
@AppFunctionSchemaDefinition(
    name = "getDeviceStateItem",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetDeviceStateItem {
    /**
     * Gets a particular device state based on the provided params.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getDeviceStateItemParams the request containing the required params to get the device
     *   state item.
     */
    suspend fun getDeviceStateItem(
        appFunctionContext: AppFunctionContext,
        getDeviceStateItemParams: GetDeviceStateItemParams,
    ): DeviceStateItemResponse
}

/** Gets storage device state. */
@AppFunctionSchemaDefinition(
    name = "getStorageDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetStorageDeviceState {
    /**
     * Gets storage device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getStorageDeviceStateParams the request containing the required params to get the
     *   device state item.
     */
    suspend fun getStorageDeviceState(
        appFunctionContext: AppFunctionContext,
        getStorageDeviceStateParams: GetStorageDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets battery device state. */
@AppFunctionSchemaDefinition(
    name = "getBatteryDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetBatteryDeviceState {
    /**
     * Gets battery device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getBatteryDeviceStateParams the request containing the required params to get the
     *   device state item.
     */
    suspend fun getBatteryDeviceState(
        appFunctionContext: AppFunctionContext,
        getBatteryDeviceStateParams: GetBatteryDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets mobile data usage device state. */
@AppFunctionSchemaDefinition(
    name = "getMobileDataUsageDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetMobileDataUsageDeviceState {
    /**
     * Gets mobile data suage device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getMobileDataUsageDeviceStateParams the request containing the required params to get
     *   the device state item.
     */
    suspend fun getMobileDataUsageDeviceState(
        appFunctionContext: AppFunctionContext,
        getMobileDataUsageDeviceStateParams: GetMobileDataUsageDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets permissions device state. */
@AppFunctionSchemaDefinition(
    name = "getPermissionsDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetPermissionsDeviceState {
    /**
     * Gets permissions device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getPermissionsDeviceStateParams the request containing the required params to get the
     *   device state item.
     */
    suspend fun getPermissionsDeviceState(
        appFunctionContext: AppFunctionContext,
        getPermissionsDeviceStateParams: GetPermissionsDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets notifications device state. */
@AppFunctionSchemaDefinition(
    name = "getNotificationsDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetNotificationsDeviceState {
    /**
     * Gets notifications device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getNotificationsDeviceStateParams the request containing the required params to get
     *   the device state item.
     */
    suspend fun getNotificationsDeviceState(
        appFunctionContext: AppFunctionContext,
        getNotificationsDeviceStateParams: GetNotificationsDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets wellbeing device state. */
@AppFunctionSchemaDefinition(
    name = "getWellbeingDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetWellbeingDeviceState {
    /**
     * Gets wellbeing device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getWellbeingDeviceStateParams the request containing the required params to get the
     *   device state item.
     */
    suspend fun getWellbeingDeviceState(
        appFunctionContext: AppFunctionContext,
        getWellbeingDeviceStateParams: GetWellbeingDeviceStateParams,
    ): DeviceStateResponse
}

/** Gets apps device states. */
@AppFunctionSchemaDefinition(
    name = "getAppsDeviceState",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetAppsDeviceState {
    /**
     * Gets apps device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getAppsDeviceStateParams the request containing the required params to get the device
     *   state item.
     */
    suspend fun getAppsDeviceState(
        appFunctionContext: AppFunctionContext,
        getAppsDeviceStateParams: GetAppsDeviceStateParams,
    ): DeviceStateResponse
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetUncategorizedDeviceStateParams"
)
class GetUncategorizedDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetUncategorizedDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetStorageDeviceStateParams"
)
class GetStorageDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetStorageDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetBatteryDeviceStateParams"
)
class GetBatteryDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetBatteryDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetMobileDataUsageDeviceStateParams"
)
class GetMobileDataUsageDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetMobileDataUsageDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetPermissionsDeviceStateParams"
)
class GetPermissionsDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetPermissionsDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetNotificationsDeviceStateParams"
)
class GetNotificationsDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetNotificationsDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetWellbeingDeviceStateParams"
)
class GetWellbeingDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetWellbeingDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.GetAppsDeviceStateParams"
)
class GetAppsDeviceStateParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
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
        other is GetAppsDeviceStateParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/** Represents the request that is passed in to get a certain device state. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.GetDeviceStateItemParams"
)
class GetDeviceStateItemParams(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The unique identifier of the setting to read. */
    @Document.StringProperty(required = true) val key: String,
    /** A list of itemization keys required to identify an itemized preference . */
    @Document.StringProperty val itemizationKeys: List<String> = emptyList(),
) {
    override fun equals(other: Any?) =
        other is GetDeviceStateItemParams &&
            key == other.key &&
            itemizationKeys == other.itemizationKeys

    override fun hashCode() = Objects.hash(key, itemizationKeys)
}

/**
 * Represents the overall state of relevant device settings, structured for consumption by an LLM.
 * This serves as the top-level response object when querying device state.
 */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateResponse")
class DeviceStateResponse(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** List of per-screen device states. */
    @Document.DocumentProperty val perScreenDeviceStates: List<PerScreenDeviceStates> = emptyList(),
    /**
     * The device's locale, represented as a BCP 47 language tag.
     *
     * Examples: "en-US", "fr-CA", "zh-Hans-CN".
     */
    @Document.StringProperty(required = true) val deviceLocale: String,
    // Additional hints for the LLM to help it understand the device / device state that aren't
    // scoped
    // to a specific screen.
    @Document.StringProperty val globalHintText: String? = null,
) {
    override fun equals(other: Any?) =
        other is DeviceStateResponse &&
            perScreenDeviceStates == other.perScreenDeviceStates &&
            deviceLocale == other.deviceLocale &&
            globalHintText == other.globalHintText

    override fun hashCode() = Objects.hash(perScreenDeviceStates, deviceLocale, globalHintText)
}

/** A list of device states, logically grouped by the Settings screen or area where they appear. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.PerScreenDeviceStates"
)
class PerScreenDeviceStates(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /**
     * Optional natural language description providing context about this group of settings. Useful
     * for the LLM to understand the purpose or scope of this screen/section. Use LLM-interpretable
     * language. Avoid internal jargon. Can include additional hints that would be interpretable by
     * the LLM
     */
    @Document.StringProperty(required = true) val description: String,
    /** Intent uri for the screen, or the nearest parent screen that makes sense. */
    @Document.StringProperty val intentUri: String? = null,
    /** List of device state items on the screen. */
    @Document.DocumentProperty val deviceStateItems: List<DeviceStateItem> = emptyList(),
) {
    override fun equals(other: Any?) =
        other is PerScreenDeviceStates &&
            description == other.description &&
            intentUri == other.intentUri &&
            deviceStateItems == other.deviceStateItems

    override fun hashCode() = Objects.hash(description, intentUri, deviceStateItems)
}

/** Represents a single device state item, structured for consumption by an LLM. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateItemResponse"
)
class DeviceStateItemResponse(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The device state item. */
    @Document.DocumentProperty(required = true) val deviceStateItem: DeviceStateItem,
) {
    override fun equals(other: Any?) =
        other is DeviceStateItemResponse && deviceStateItem == other.deviceStateItem

    override fun hashCode() = Objects.hash(deviceStateItem)
}

/** Class for a device state item. */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateItem")
class DeviceStateItem(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /**
     * A String identifying this specific setting. MUST be designed to be understood by LLMs Note
     * that this will be removed in an upcoming update in favour of just relying on `purpose`.
     */
    @Document.StringProperty(required = true) val key: String,
    /** A String identifying this specific setting. MUST be designed to be understood by LLMs */
    @Document.StringProperty val purpose: String? = null,
    /**
     * The human-readable name or label for this setting as it appears *exactly* in the Settings UI,
     * localized according to `deviceStateLocale`. Example: "Wi-Fi", "Brightness level", "Show
     * notifications". Optional: Might not always be available or easily scrapable. Primarily useful
     * for display verification or showing back to the user, less critical for LLM logic than the
     * `purpose`. Assumes `LocalizedString` handles the actual localized text.
     */
    @Document.DocumentProperty val name: LocalizedString? = null,
    /**
     * This JSON string serves as a direct pass-through to LLM It is intended only for consumption
     * by the LLM, and developers are not expected to parse it manually. This is optional - we don't
     * necessarily have the value but we still want to let Gemini know the item exists so it can
     * point the user towards the relevant screen
     */
    @Document.StringProperty val jsonValue: String? = null,
    /** The last time this setting was retrieved real time (not from the cache). */
    @Document.LongProperty val lastUpdatedEpochMillis: Long? = null,
    /**
     * Optional natural language hints or instructions for the LLM on how to interpret the
     * preference and its`jsonValue`. This can clarify units, scales, valid ranges, relationships to
     * other settings, or constraints. Examples:
     * - "Value is a percentage (0-100)."
     * - "Enum values: 'ENABLED', 'DISABLED', 'ASK'."
     * - "Scale from 0 (off) to 10 (max)."
     * - "This setting is only effective if 'master.switch.key' is enabled."
     * - "This setting can be changed by the device care app, package name: com.oem.devicecare" Use
     *   clear, LLM-interpretable language.
     */
    @Document.StringProperty val hintText: String? = null,
) {
    override fun equals(other: Any?) =
        other is DeviceStateItem &&
            key == other.key &&
            purpose == other.purpose &&
            name == other.name &&
            jsonValue == other.jsonValue &&
            lastUpdatedEpochMillis == other.lastUpdatedEpochMillis &&
            hintText == other.hintText

    override fun hashCode() =
        Objects.hash(key, purpose, name, jsonValue, lastUpdatedEpochMillis, hintText)
}

/** Class for a localized string. */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.LocalizedString")
class LocalizedString(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** English version of the string. */
    @Document.StringProperty(required = true) val english: String,
    /** Localized version of the string. */
    @Document.StringProperty val localized: String? = null,
) {
    override fun equals(other: Any?) =
        other is LocalizedString && english == other.english && localized == other.localized

    override fun hashCode() = Objects.hash(english, localized)
}
