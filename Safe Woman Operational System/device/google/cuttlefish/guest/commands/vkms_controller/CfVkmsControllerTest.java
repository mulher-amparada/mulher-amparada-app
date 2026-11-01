/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.cuttlefish.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.log.LogUtil.CLog;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner;
import com.android.tradefed.testtype.junit4.BaseHostJUnit4Test;
import com.android.tradefed.util.CommandResult;
import com.android.tradefed.util.CommandStatus;
import com.android.tradefed.util.RunUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Host-side test verifying the native behavior of the guest
 * "vkms_controller" binary via adb shell.
 */
@RunWith(DeviceJUnit4ClassRunner.class)
public class CfVkmsControllerTest extends BaseHostJUnit4Test {
    private static final String CMD_PREFIX = "vkms_controller ";

    @Before
    public void setUp() throws Exception {
        // Ensure device is rooted so we can interact with configfs
        getDevice().enableAdbRoot();
        // Ensure a clean state before each test
        runTargetCommand("reset");
    }

    @After
    public void tearDown() throws Exception {
        runTargetCommand("reset");
    }

    private CommandResult runTargetCommand(String commandArgs) throws Exception {
        ITestDevice device = getDevice();
        String fullCommand = CMD_PREFIX + commandArgs;
        CLog.i("Running target command: " + fullCommand);

        CommandResult result = device.executeShellV2Command(fullCommand);
        if (result.getStatus() != CommandStatus.SUCCESS) {
            CLog.e("Target command failed: " + result.getStderr());
        }
        return result;
    }

    @Test
    public void testUsageAndInvalidArgs() throws Exception {
        CommandResult result = runTargetCommand(""); // No args
        assertEquals("Missing args should fail", CommandStatus.FAILED, result.getStatus());
        assertTrue("Must print usage", result.getStdout().contains("Usage:"));

        result = runTargetCommand("invalid_command");
        assertEquals("Invalid command should fail", CommandStatus.FAILED, result.getStatus());
        assertTrue("Must print usage", result.getStdout().contains("Usage:"));
    }

    @Test
    public void testListPresets() throws Exception {
        CommandResult result = runTargetCommand("list-presets");
        assertEquals("List presets failed: " + result.getStderr(),
                     CommandStatus.SUCCESS, result.getStatus());

        String stdout = result.getStdout();
        assertTrue("Presets must include REDRIX", stdout.contains("REDRIX"));
        assertTrue("Presets must include HP_Spectre32_4K_DP", stdout.contains("HP_Spectre32_4K_DP"));
        assertTrue("Presets must include ACI_9155_ASUS_VH238_HDMI", stdout.contains("ACI_9155_ASUS_VH238_HDMI"));
    }

    @Test
    public void testSetupGeneric() throws Exception {
        // HandleSetupGeneric branch
        CommandResult result = runTargetCommand("setup 2");
        assertEquals("Setup generic failed", CommandStatus.SUCCESS, result.getStatus());

        result = runTargetCommand("list-displays --json");
        String jsonOut = result.getStdout();
        assertTrue("Must have generic edp", jsonOut.contains("REDRIX"));
        assertTrue("Must have generic dp", jsonOut.contains("HP_Spectre32_4K_DP"));
    }

    @Test
    public void testSetupScreensAndListDisplaysJSON() throws Exception {
        // HandleSetupScreens branch
        CommandResult result = runTargetCommand("setup " +
                "--screen=name=REDRIX " +
                "--screen=name=ACI_9155_ASUS_VH238_HDMI,planes=2,enabled=false");
        assertEquals("Setup screens failed: " + result.getStderr(),
                     CommandStatus.SUCCESS, result.getStatus());

        // Wait for SurfaceFlinger to pick up the new displays (polling)
        boolean displaysDetected = false;
        for (int i = 0; i < 10; i++) {
            result = runTargetCommand("list-displays --json");
            if (result.getStatus() == CommandStatus.SUCCESS) {
                String jsonOut = result.getStdout();
                if (jsonOut.contains("\"display_name\" : \"REDRIX\"")) {
                    displaysDetected = true;
                    break;
                }
            }
            RunUtil.getDefault().sleep(1000);
        }
        assertTrue("Displays were not detected by SurfaceFlinger in time", displaysDetected);

        // Test JSON List Displays
        result = runTargetCommand("list-displays --json");
        assertEquals("List displays failed", CommandStatus.SUCCESS, result.getStatus());

        String jsonOut = result.getStdout();

        // Ensure the JSON response formed properly containing both displays
        assertTrue("JSON output missing REDRIX: " + jsonOut,
                   jsonOut.contains("\"display_name\" : \"REDRIX\""));
        assertTrue("JSON output missing ASUS_VH238_HDMI: " + jsonOut,
                   jsonOut.contains("\"display_name\" : \"ACI_9155_ASUS_VH238_HDMI\""));

        // REDRIX should be connected (default behavior)
        assertTrue("REDRIX should be connected",
                   jsonOut.contains("\"status\" : \"Connected\""));

        // ASUS should be disabled from the `--screen` argument
        assertTrue("ASUS should be disconnected",
                   jsonOut.contains("\"status\" : \"Disconnected\""));
    }

    @Test
    public void testHotplugToggle() throws Exception {
        // Setup a standard basic generic display
        CommandResult result = runTargetCommand("setup 1");
        assertEquals("Setup screens failed: " + result.getStderr(),
                     CommandStatus.SUCCESS, result.getStatus());

        // Ensure initial state is Connected
        result = runTargetCommand("list-displays --json");
        assertTrue("JSON must report Connected: " + result.getStdout(),
                   result.getStdout().contains("\"status\" : \"Connected\""));

        // Hotplug OFF
        result = runTargetCommand("hotplug 0 disconnected");
        assertEquals("Hotplug off failed", CommandStatus.SUCCESS, result.getStatus());

        // Validate Native JSON State change
        result = runTargetCommand("list-displays --json");
        assertTrue("JSON must report Disconnected: " + result.getStdout(),
                   result.getStdout().contains("\"status\" : \"Disconnected\""));

        // Hotplug ON
        result = runTargetCommand("hotplug 0 connected");
        assertEquals("Hotplug on failed", CommandStatus.SUCCESS, result.getStatus());

        // Validate Native JSON State restored
        result = runTargetCommand("list-displays --json");
        assertTrue("JSON must report Connected again: " + result.getStdout(),
                   result.getStdout().contains("\"status\" : \"Connected\""));
    }

    @Test
    public void testHotplugInvalidStateAndID() throws Exception {
        // Error handling branches in hotplug
        CommandResult result = runTargetCommand("hotplug");
        assertNotEquals("Should fail with missing args", CommandStatus.SUCCESS, result.getStatus());

        // Setup so state exists
        runTargetCommand("setup 1");

        result = runTargetCommand("hotplug 999 connected");
        assertNotEquals("Should fail with invalid ID", CommandStatus.SUCCESS, result.getStatus());

        result = runTargetCommand("hotplug invalid_id connected");
        assertNotEquals("Should fail parsing ID", CommandStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void testTeardown() throws Exception {
        runTargetCommand("setup 1");
        CommandResult result = runTargetCommand("reset");
        assertEquals(CommandStatus.SUCCESS, result.getStatus());
    }
}

