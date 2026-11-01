package com.mulheres.profileadmin;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;

/** Completes setup of the Mulher Amparada managed profile. */
public final class ProfileProvisioningActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DevicePolicyManager manager = getSystemService(DevicePolicyManager.class);
        ComponentName admin = new ComponentName(this, MulherAmparadaProfileAdminReceiver.class);
        if (manager != null && manager.isProfileOwnerApp(getPackageName())) {
            manager.setProfileName(admin, "Mulher Amparada");
            manager.setProfileEnabled(admin);
        }
        finish();
    }
}
