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

import androidx.annotation.StringDef
import androidx.appsearch.annotation.Document
import java.util.Objects

/** Gets device state metadata. */
@AppFunctionSchemaDefinition(
    name = "getDeviceStateMetadata",
    version = 1,
    category = DEVICE_STATE_CATEGORY,
)
interface GetDeviceStateMetadata {
    /**
     * Gets metadata for the device states.
     *
     * @param appFunctionContext The AppFunction execution context.
     * @param getDeviceStateMetadataParams the request containing the required params to get device
     *   state metadata.
     */
    suspend fun getDeviceStateMetadata(
        appFunctionContext: AppFunctionContext,
        getDeviceStateMetadataParams: GetDeviceStateMetadataParams,
    ): DeviceStateMetadataResponse
}

/** Represents the request that is passed in to get the device state metadata. */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.GetDeviceStateMetadataParams"
)
class GetDeviceStateMetadataParams(
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
        other is GetDeviceStateMetadataParams &&
            requestInitiatedWhileUnlocked == other.requestInitiatedWhileUnlocked

    override fun hashCode() = Objects.hash(requestInitiatedWhileUnlocked)
}

/**
 * Represents the metadata of relevant device settings, structured for consumption by an LLM. This
 * serves as the top-level response object when querying device state schema.
 */
@Document(
    name =
        "com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateMetadataResponse"
)
class DeviceStateMetadataResponse(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** List of per-screen device states. */
    @Document.DocumentProperty val perScreenMetadata: List<PerScreenMetadata> = emptyList(),
    /**
     * The device's locale, represented as a BCP 47 language tag.
     *
     * Examples: "en-US", "fr-CA", "zh-Hans-CN".
     */
    @Document.StringProperty(required = true) val deviceLocale: String,
    /**
     * Additional hints for the LLM to help it understand the device / device state that aren't
     * scoped to a specific screen.
     */
    @Document.StringProperty val globalHintText: String? = null,
    /**
     * Provides details for settings that are duplicated across multiple items. For example,
     * notification settings are duplicated for each app.
     *
     * Each item in the list represents an "itemization type" (e.g., "package" for per-app or "sim"
     * for per-SIM settings). The `key` in [ItemizationType] is the itemization type, and the
     * `values` list contains all items of that type on the device.
     *
     * For the "package" itemization type, the `values` list would contain an [ItemizationDetail]
     * for each app, where `key` is the package name and `value` is the user-friendly app name.
     *
     * A [PerScreenMetadata] with a non-null `itemizationType` refers to a generic screen whose
     * settings apply to every item in the corresponding list in this field.
     */
    @Document.DocumentProperty val itemizationTypes: List<ItemizationType> = emptyList(),
) {
    override fun equals(other: Any?) =
        other is DeviceStateMetadataResponse &&
            perScreenMetadata == other.perScreenMetadata &&
            deviceLocale == other.deviceLocale &&
            globalHintText == other.globalHintText &&
            itemizationTypes == other.itemizationTypes

    override fun hashCode() =
        Objects.hash(perScreenMetadata, deviceLocale, globalHintText, itemizationTypes)
}

/**
 * A list of screen metadata, logically grouped by the Settings screen or area where they appear.
 */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.PerScreenMetadata")
class PerScreenMetadata(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /**
     * Optional natural language description providing context about this group of settings. Useful
     * for the LLM to understand the purpose or scope of this screen/section. Use LLM-interpretable
     * language. Avoid internal jargon. Can include additional hints that would be interpretable by
     * the LLM
     */
    @Document.StringProperty(required = true) val description: String,
    /**
     * Intent uri for the screen, or the nearest parent screen that makes sense.
     *
     * <p>For the link to be functional, the provided [intentUri] <strong>must</strong> be
     * resolvable by an [android.content.Intent] with the action
     * [android.content.Intent#ACTION_VIEW].
     */
    @Document.StringProperty val intentUri: String? = null,
    /**
     * If set, this indicates that this is a generic metadata representation of a screen that is
     * duplicated per item e.g. Sim settings screen and Apps settings screens are duplicated per
     * app/sim, but only one screen metadata will be returned.
     *
     * For non generic screens, this will be set to null.
     *
     * For generic metadata screens, this explains the type items the screen is deduplicating, e.g.
     * that it's a per-package screen in case of the App settings screen.
     */
    @Document.StringProperty val itemizationType: String? = null,
    /** List of device state items on the screen. */
    @Document.DocumentProperty
    val deviceStateItemsMetadata: List<DeviceStateItemMetadata> = emptyList(),
) {
    override fun equals(other: Any?) =
        other is PerScreenMetadata &&
            description == other.description &&
            intentUri == other.intentUri &&
            itemizationType == other.itemizationType &&
            deviceStateItemsMetadata == other.deviceStateItemsMetadata

    override fun hashCode() =
        Objects.hash(description, intentUri, itemizationType, deviceStateItemsMetadata)
}

/** Class for a device state item. */
@Document(
    name = "com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateItemMetadata"
)
class DeviceStateItemMetadata(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /**
     * A unique key identifying this specific setting. This should be passed in to `setDeviceState`
     * app function or one of its variants if the caller wishes to update this preference's value.
     */
    @Document.StringProperty(required = true) val key: String,
    /** A String identifying this specific setting. MUST be designed to be understood by LLMs */
    @Document.StringProperty(required = true) val purpose: String,
    /** Name from the UI - optional. */
    @Document.DocumentProperty val name: LocalizedString? = null,
    /**
     * The sensitivity of the preference, could be one of "REQUIRES_CONFIRMATION" or
     * "MUST_PROVIDE_UNDO" or can be null.
     */
    @param:Sensitivity @Document.StringProperty val sensitivity: String? = null,
    /** Whether this item is settable or not. */
    @Document.BooleanProperty val writable: Boolean? = null,
    /**
     * A string representation of the possible values this setting can be set to. The following are
     * example values, note that it does not have to be strictly limited to the provided format in
     * the examples:
     * - For wifi toggle possibleValue="{\"type\": \"Toggle\", \"valueType'\": \"BOOLEAN\" }"
     * - For screen timeout possibleValue="{\"type\": \"PredeterminedFinite\", \"valueType\":
     *   \"INTEGER\", \"values\": [ \"15\", \"30\", \"60\", \"120\", \"300\"]}",
     * - For brightness levels possibleValue="{\"type\": \"Range\", \"valueType\": \"INTEGER\",
     *   \"start\": \"0\", \"end\": \"100\", \"step\": \"1\" }".
     */
    @Document.StringProperty val possibleValues: String? = null,

    /**
     * Optional natural language hints or instructions for the LLM on how to interpret the `key` and
     * `possibleValues`. This can clarify units, scales, valid ranges, relationships to other
     * settings, or constraints. Examples:
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
        other is DeviceStateItemMetadata &&
            key == other.key &&
            purpose == other.purpose &&
            name == other.name &&
            sensitivity == other.sensitivity &&
            writable == other.writable &&
            possibleValues == other.possibleValues &&
            hintText == other.hintText

    override fun hashCode() =
        Objects.hash(key, purpose, name, sensitivity, writable, possibleValues, hintText)
}

/** The sensitivity level of a certain setting. */
@StringDef(Sensitivity.MUST_PROVIDE_UNDO, Sensitivity.REQUIRES_CONFIRMATION)
@Retention(AnnotationRetention.SOURCE)
annotation class Sensitivity {
    companion object {
        /**
         * The agent must provide the option to the user to undo the change that was just executed.
         */
        const val MUST_PROVIDE_UNDO = "MUST_PROVIDE_UNDO"
        /**
         * The agent can not act on its own to change the setting, it has to provide the UI for the
         * user to change it
         */
        const val REQUIRES_CONFIRMATION = "REQUIRES_CONFIRMATION"
    }
}

/** Represents a type of itemization. */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.ItemizationType")
class ItemizationType(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /** The key for this itemization type, e.g., "package" or "sim". */
    @Document.StringProperty(required = true) val key: String,
    /** The list of items for this itemization type. */
    @Document.DocumentProperty val values: List<ItemizationDetail> = emptyList(),
) {
    override fun equals(other: Any?) =
        other is ItemizationType && key == other.key && values == other.values

    override fun hashCode() = Objects.hash(key, values)
}

/** Class for a device state itemization detail. */
@Document(name = "com.google.android.appfunctions.schema.common.v1.devicestate.ItemizationDetail")
class ItemizationDetail(
    @Document.Namespace val namespace: String = "", // unused
    @Document.Id val id: String = "", // unused
    /**
     * The key identifying this specific itemization detail, e.g. for package-based itemizations it
     * would be the package name. Note that these should be unique across all itemizationTypes.
     */
    @Document.StringProperty(required = true) val key: String,
    /**
     * The equivalent value for the provided key, e.g. the app name of the package name. Note that
     * these should be unique across all itemizationTypes
     */
    @Document.StringProperty(required = true) val value: String,
) {
    override fun equals(other: Any?) =
        other is ItemizationDetail && key == other.key && value == other.value

    override fun hashCode() = Objects.hash(key, value)
}
