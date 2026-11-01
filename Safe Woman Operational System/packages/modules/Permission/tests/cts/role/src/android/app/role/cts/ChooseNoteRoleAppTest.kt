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

package android.app.role.cts

import android.content.Intent
import android.platform.test.rule.NotesRoleManagerRule
import android.platform.uiautomatorhelpers.WaitUtils.ensureThat
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By.text
import com.android.compatibility.common.util.UiAutomatorUtils2.getUiDevice
import com.android.compatibility.common.util.UiAutomatorUtils2.waitFindObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChooseNoteRoleAppTest {

    @[Rule JvmField]
    val rule = NotesRoleManagerRule(requiredNotesRoleHolderPackage = NOTES_APP_PACKAGE_NAME)

    @Before
    fun setUp() {
        rule.utils.clearRoleHolder()
        InstrumentationRegistry.getInstrumentation()
            .context
            .startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
    }

    @After
    fun after() {
        getUiDevice().pressHome()
    }

    @Test
    fun chooseNoteRoleHolderApp() {
        ensureThat { rule.utils.getRoleHolderPackageName().isEmpty() }

        // Scroll to "Notes app" item in Default apps screen and click on it
        waitFindObject(text("Notes app")).click()
        // Scroll to "CtsDefaultNotesApp" item and click on it
        waitFindObject(text("CtsDefaultNotesApp")).click()

        assertEquals(rule.utils.getRoleHolderPackageName(), NOTES_APP_PACKAGE_NAME)
    }

    private companion object {
        const val NOTES_APP_PACKAGE_NAME = "com.android.cts.notesapp"
    }
}
