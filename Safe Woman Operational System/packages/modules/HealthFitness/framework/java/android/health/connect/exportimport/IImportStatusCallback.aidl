package android.health.connect.exportimport;

import android.health.connect.exportimport.ImportStatus;
import android.health.connect.aidl.HealthConnectExceptionParcel;

/**
 * Callback for {@link HealthConnectManager#getImportStatus}
 *
 * @hide
 */
interface IImportStatusCallback {
    // Called on a successful operation
    oneway void onResult(in ImportStatus status);
    // Called when an error is hit
    oneway void onError(in HealthConnectExceptionParcel exception);
}