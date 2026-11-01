/*
 * Copyright (C) 2024 The Android Open Source Project
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

package android.healthconnect.tests.documentprovider.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Handles requests from test cases to configure the test document provider. */
public final class DocumentProviderActivityRequestHandler {
    /** Handles requests from test cases to configure the test document provider. */
    public static void handleRequest(Activity activity) {
        Intent response = new Intent(DocumentProviderIntent.RESPONSE);
        try {
            handleRequestFromBundle(activity.getIntent().getExtras());
        } catch (Exception e) {
            response.putExtra(DocumentProviderIntent.EXCEPTION, e);
        } finally {
            activity.sendBroadcast(response);
            activity.finish();
        }
    }

    private static void handleRequestFromBundle(Bundle bundle) {
        String queryType = bundle.getString(DocumentProviderIntent.ACTION_TYPE);
        switch (queryType) {
            case DocumentProviderIntent.SET_DOCUMENT_PROVIDER_THROWS_EXCEPTION:
                FakeDocumentProviderRoots.INSTANCE.setThrowsException();
                break;
            case DocumentProviderIntent.CLEAR_DOCUMENT_PROVIDER_ROOTS:
                FakeDocumentProviderRoots.INSTANCE.clear();
                break;
            case DocumentProviderIntent.ADD_DOCUMENT_PROVIDER_ROOT:
                FakeDocumentProviderRoots.INSTANCE.addRoot(bundle);
                break;
            default:
                throw new IllegalStateException(
                        "Unknown query received from launcher app: " + queryType);
        }
    }
}
