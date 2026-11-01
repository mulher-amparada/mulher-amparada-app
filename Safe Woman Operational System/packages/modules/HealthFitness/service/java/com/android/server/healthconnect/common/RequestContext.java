/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.server.healthconnect.common;

import android.os.Binder;
import android.os.UserHandle;

/**
 * Contains state associated with the current request.
 *
 * @hide
 */
public class RequestContext {
    private final int mUid;
    private final int mPid;
    private final UserHandle mCallingUser;

    /**
     * Create a new {@code RequestContext}.
     *
     * <p>Must be called on the request thread, i.e. the binder thread that is handling the current
     * request.
     *
     * @return A {@code RequestContext} instance.
     */
    public static RequestContext create() {
        return new RequestContext(
                Binder.getCallingUid(), Binder.getCallingPid(), Binder.getCallingUserHandle());
    }

    /** The user who owns the process which the current request originated from. */
    public UserHandle getCallingUser() {
        return mCallingUser;
    }

    /** The process ID of the caller. */
    public int getCallingProcessId() {
        return mPid;
    }

    /** The system assigned UID of the calling application. */
    public int getCallingApplicationUid() {
        return mUid;
    }

    private RequestContext(int uid, int pid, UserHandle callingUser) {
        this.mUid = uid;
        this.mPid = pid;
        this.mCallingUser = callingUser;
    }
}
