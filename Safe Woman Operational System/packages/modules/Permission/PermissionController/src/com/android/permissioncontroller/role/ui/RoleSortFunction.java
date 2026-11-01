/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.permissioncontroller.role.ui;

import android.content.Context;
import android.icu.text.Collator;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import com.android.permissioncontroller.permission.utils.Utils;

import kotlin.jvm.functions.Function1;

import java.util.Comparator;

/**
 * A function for
 * {@link androidx.lifecycle.Transformations#map(androidx.lifecycle.LiveData, Function1)}
 * that sorts a live data for role.
 */
public class RoleSortFunction extends ListLiveDataSortFunction<RoleApplicationItem> {

    public RoleSortFunction(@NonNull Context context) {
        super(createComparator(context));
    }

    private static Comparator<RoleApplicationItem> createComparator(@NonNull Context context) {
        Collator collator = Collator.getInstance(context.getResources().getConfiguration()
                .getLocales().get(0));
        Comparator<RoleApplicationItem> labelComparator = Comparator.comparing(item ->
                Utils.getAppLabel(item.getApplicationInfo(), context), collator);
        Comparator<RoleApplicationItem> userIdComparator = Comparator.comparingInt(item
                -> UserHandle.getUserHandleForUid(item.getApplicationInfo().uid).getIdentifier());
        return labelComparator.thenComparing(userIdComparator);
    }
}
