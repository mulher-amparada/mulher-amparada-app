package com.android.managedprovisioning.common

import com.android.managedprovisioning.annotations.LegacyApi
import javax.inject.Inject
import com.android.managedprovisioning.flags.FeatureFlags as AconfigFlags
import com.android.managedprovisioning.flags.FeatureFlagsImpl as DefaultAconfigFlags


interface Flags : AconfigFlags {
    @Deprecated(
        "Static version is reserved for edge-cases where DI could not be " +
                "easily achieved and as such should be avoided. " +
                "Consider using injected version whenever possible."
    )
    @LegacyApi
    companion object : Flags by DefaultFlags()
}

class DefaultFlags(
    private val aconfigFlags: AconfigFlags,
) : Flags, AconfigFlags by aconfigFlags {
    @Inject constructor() : this(DefaultAconfigFlags())

    override fun isCosmicRayEnabled(): Boolean = aconfigFlags.isCosmicRayEnabled
}
