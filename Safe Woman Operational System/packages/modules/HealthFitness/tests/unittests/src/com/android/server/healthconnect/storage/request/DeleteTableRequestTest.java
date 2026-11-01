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

package com.android.server.healthconnect.storage.request;

import static com.android.server.healthconnect.storage.utils.WhereClauses.LogicalOperator.AND;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.server.healthconnect.storage.utils.WhereClauses;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeleteTableRequestTest {

    @Test
    public void testAddNestedWhereClauses() {
        DeleteTableRequest request =
                new DeleteTableRequest("tableName")
                        .addExtraWhereClauses(
                                new WhereClauses(AND).addWhereEqualsClause("foo", "value"));
        assertThat(request.getDeleteCommand())
                .isEqualTo("DELETE FROM tableName WHERE foo = 'value'");
    }

    @Test
    public void testNoNestedWhereClauses() {
        DeleteTableRequest request = new DeleteTableRequest("tableName").setId("idColumn", "id");
        assertThat(request.getDeleteCommand())
                .isEqualTo("DELETE FROM tableName WHERE idColumn IN ('id')");
    }

    @Test
    public void testAddNestedWhere_withOtherClauses() {
        DeleteTableRequest request =
                new DeleteTableRequest("tableName")
                        .setId("idColumn", "id")
                        .addExtraWhereClauses(
                                new WhereClauses(AND).addWhereEqualsClause("foo", "value"));
        assertThat(request.getDeleteCommand())
                .isEqualTo("DELETE FROM tableName WHERE foo = 'value' AND idColumn IN ('id')");
    }
}
