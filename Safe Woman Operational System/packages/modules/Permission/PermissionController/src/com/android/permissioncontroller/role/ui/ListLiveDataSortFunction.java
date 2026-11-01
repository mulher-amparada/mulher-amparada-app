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

package com.android.permissioncontroller.role.ui;

import androidx.annotation.NonNull;

import kotlin.jvm.functions.Function1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A function for
 * {@link androidx.lifecycle.Transformations#map(androidx.lifecycle.LiveData, Function1)}
 * that sorts a live data for a list.
 *
 * @param <T> the type of the list elements
 */
public class ListLiveDataSortFunction<T> implements Function1<List<T>, List<T>> {

    @NonNull
    private final Comparator<T> mComparator;

    public ListLiveDataSortFunction(@NonNull Comparator<T> comparator) {
        mComparator = comparator;
    }

    @Override
    public List<T> invoke(List<T> items) {
        List<T> sortedItems = new ArrayList<>(items);
        sortedItems.sort(mComparator);
        return sortedItems;
    }
}
