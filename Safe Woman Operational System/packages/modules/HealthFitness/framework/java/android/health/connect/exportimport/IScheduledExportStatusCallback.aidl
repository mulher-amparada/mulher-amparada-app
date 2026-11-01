package android.health.connect.exportimport;

import android.health.connect.exportimport.ScheduledExportStatus;
import android.health.connect.aidl.HealthConnectExceptionParcel;

/**
 * Callback for {@link HealthConnectManager#getScheduledExportStatus}
 *
 * @hide
 */
interface IScheduledExportStatusCallback {
    // Called on a successful operation
    oneway void onResult(in ScheduledExportStatus status);
    // Called when an error is hit
    oneway void onError(in HealthConnectExceptionParcel exception);
}