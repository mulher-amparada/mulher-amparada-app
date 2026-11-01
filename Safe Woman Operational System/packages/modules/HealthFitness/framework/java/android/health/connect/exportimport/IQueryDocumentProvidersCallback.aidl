package android.health.connect.exportimport;

import android.health.connect.exportimport.ExportImportDocumentProvider;
import android.health.connect.aidl.HealthConnectExceptionParcel;

/**
 * Callback for {@link HealthConnectManager#queryDocumentProviders}
 *
 * @hide
 */
interface IQueryDocumentProvidersCallback {
    // Called on a successful operation
    oneway void onResult(in List<ExportImportDocumentProvider> providers);
    // Called when an error is hit
    oneway void onError(in HealthConnectExceptionParcel exception);
}
