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

/** Intent extras to configure the test document provider. */
public final class DocumentProviderIntent {
    public static final String ACTION_TYPE = "android.healthconnect.tests.ACTION_TYPE";
    public static final String SET_DOCUMENT_PROVIDER_THROWS_EXCEPTION =
            "android.healthconnect.tests.SET_DOCUMENT_PROVIDER_THROWS_EXCEPTION";
    public static final String CLEAR_DOCUMENT_PROVIDER_ROOTS =
            "android.healthconnect.tests.CLEAR_DOCUMENT_PROVIDER_ROOTS";
    public static final String ADD_DOCUMENT_PROVIDER_ROOT =
            "android.healthconnect.tests.ADD_DOCUMENT_PROVIDER_ROOT";
    public static final String RESPONSE = "android.healthconnect.tests.RESPONSE";
    public static final String EXCEPTION = "android.healthconnect.tests.EXCEPTION";

    private DocumentProviderIntent() {}
}
