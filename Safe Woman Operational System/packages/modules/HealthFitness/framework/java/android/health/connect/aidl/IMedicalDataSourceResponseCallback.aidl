package android.health.connect.aidl;

import android.health.connect.aidl.HealthConnectExceptionParcel;
import android.health.connect.datatypes.MedicalDataSource;

/**
 * Callback for {@link IHealthConnectService#createMedicalDataSource}
 *
 * @hide
 */
interface IMedicalDataSourceResponseCallback {
    // Called on a successful operation
    oneway void onResult(in MedicalDataSource parcel);
    // Called when an error is hit
    oneway void onError(in HealthConnectExceptionParcel exception);
}
