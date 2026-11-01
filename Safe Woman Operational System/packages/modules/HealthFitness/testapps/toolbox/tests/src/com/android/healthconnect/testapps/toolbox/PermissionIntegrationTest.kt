package com.android.healthconnect.testapps.toolbox

import android.health.connect.HealthConnectManager
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import org.junit.Test

class PermissionIntegrationTest {

    @Test
    fun toolboxAppShouldRequestAllHealthPermissions() {
        val context = InstrumentationRegistry.getInstrumentation().context
        Truth.assertThat(Constants.HEALTH_PERMISSIONS.sorted())
            .isEqualTo(HealthConnectManager.getHealthPermissions(context).sorted())
    }

    @Test
    fun toolboxAppShouldRequestAllMedicalPermissions() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val allPermissions =
            Constants.MEDICAL_PERMISSIONS +
                Constants.FITNESS_PERMISSIONS +
                Constants.ADDITIONAL_PERMISSIONS
        Truth.assertThat(allPermissions.sorted())
            .isEqualTo(HealthConnectManager.getHealthPermissions(context).sorted())
    }
}
