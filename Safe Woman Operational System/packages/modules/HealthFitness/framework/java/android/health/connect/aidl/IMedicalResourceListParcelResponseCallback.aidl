package android.health.connect.aidl;

import android.health.connect.aidl.HealthConnectExceptionParcel;
import android.health.connect.aidl.MedicalResourceListParcel;
import android.health.connect.datatypes.MedicalResource;

/**
 * Callback for {@link IHealthConnectService#upsertMedicalResourcesFromRequestsParcel}
 *
 * @hide
 */
interface IMedicalResourceListParcelResponseCallback {
    // Called on a successful operation
    oneway void onResult(in MedicalResourceListParcel result);
    // Called when an error is hit
    oneway void onError(in HealthConnectExceptionParcel exception);
}
