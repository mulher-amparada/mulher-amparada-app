/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CreateIndexRequestTest {

    @Test
    public void testCreateIndexGetCommand_withUnique() {
        CreateIndexRequest createIndexRequest =
                new CreateIndexRequest("sample_table", "sample_idx", true, List.of("col1"));

        assertThat(createIndexRequest.getCommand())
                .isEqualTo("CREATE UNIQUE INDEX sample_idx ON sample_table (col1)");
    }

    @Test
    public void testCreateIndexGetCommand_withoutUnique() {
        CreateIndexRequest createIndexRequest =
                new CreateIndexRequest("sample_table", "sample_idx", false, List.of("col1"));

        assertThat(createIndexRequest.getCommand())
                .isEqualTo("CREATE INDEX sample_idx ON sample_table (col1)");
    }

    @Test
    public void testCreateIndexGetCommand_uniqueMultipleColumns() {
        CreateIndexRequest createIndexRequest =
                new CreateIndexRequest(
                        "sample_table", "sample_idx", true, List.of("col1", "col2", "col3"));

        assertThat(createIndexRequest.getCommand())
                .isEqualTo("CREATE UNIQUE INDEX sample_idx ON sample_table (col1, col2, col3)");
    }

    @Test
    public void testCreateIndexGetCommand_uniqueNoColumns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateIndexRequest("sample_table", "sample_idx", true, List.of()));
    }
}
