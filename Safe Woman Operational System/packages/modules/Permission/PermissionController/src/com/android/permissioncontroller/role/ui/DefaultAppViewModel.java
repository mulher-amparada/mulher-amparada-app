/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.app.AppOpsManager;
import android.app.Application;
import android.app.role.RoleManager;
import android.app.voiceinteraction.VoiceInteractionManager;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.permissioncontroller.role.utils.RoleUiBehaviorUtils;
import com.android.permissioncontroller.role.utils.UserUtils;
import com.android.role.controller.model.Role;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link ViewModel} for a default app.
 */
public class DefaultAppViewModel extends AndroidViewModel {

    private static final String LOG_TAG = DefaultAppViewModel.class.getSimpleName();

    @NonNull
    private final Role mRole;
    @NonNull
    private final UserHandle mUser;

    @NonNull
    private final LiveData<List<RoleApplicationItem>> mRecommendedLiveData;

    @NonNull
    private final LiveData<List<RoleApplicationItem>> mLiveData;

    @NonNull
    private final ManageRoleHolderStateLiveData mManageRoleHolderStateLiveData =
            new ManageRoleHolderStateLiveData();

    private final AppOpsManager mAppOpsManager;
    private final VoiceInteractionManager mVoiceInteractionManager;

    public DefaultAppViewModel(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Application application) {
        super(application);

        mAppOpsManager = application.getSystemService(AppOpsManager.class);
        mVoiceInteractionManager =
                android.permission.flags.Flags.assistSettingsPrivacyImprovementsEnabled()
                        ? application.getSystemService(VoiceInteractionManager.class) : null;

        mRole = role;
        // If EXCLUSIVITY_PROFILE_GROUP this user should be profile parent
        mUser = role.getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP
                ? UserUtils.getProfileParentOrSelf(user, application)
                : user;

        RoleLiveData userLiveData = createRoleLiveData(role, mUser, application);

        RoleSortFunction sortFunction = new RoleSortFunction(application);
        LiveData<List<RoleApplicationItem>> liveData;
        if (role.getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP) {
            // Context user might be work profile, ensure we get a non-null UserHandle if work
            // profile exists. getWorkProfile returns null if context user is work profile.
            UserHandle workProfile  = UserUtils.getWorkProfileOrSelf(application);
            if (workProfile != null) {
                RoleLiveData workLiveData = createRoleLiveData(role, workProfile, application);
                liveData = Transformations.map(new MergeRoleLiveData(userLiveData, workLiveData),
                        sortFunction);
            } else {
                liveData = Transformations.map(userLiveData, sortFunction);
            }
        } else {
            liveData = Transformations.map(userLiveData, sortFunction);
        }
        Predicate<RoleApplicationItem> recommendedApplicationFilter =
                RoleUiBehaviorUtils.getRecommendedApplicationFilter(role, application);
        mRecommendedLiveData = Transformations.map(liveData,
                new ListLiveDataFilterFunction<>(recommendedApplicationFilter));
        mLiveData = Transformations.map(liveData,
                new ListLiveDataFilterFunction<>(recommendedApplicationFilter.negate()));
    }

    @NonNull
    public LiveData<List<RoleApplicationItem>> getRecommendedLiveData() {
        return mRecommendedLiveData;
    }

    @NonNull
    public LiveData<List<RoleApplicationItem>> getLiveData() {
        return mLiveData;
    }

    @NonNull
    public ManageRoleHolderStateLiveData getManageRoleHolderStateLiveData() {
        return mManageRoleHolderStateLiveData;
    }

    /**
     * Set an application as the default app.
     *
     * @param packageName the package name of the application
     */
    public void setDefaultApp(@NonNull String packageName, @NonNull UserHandle user) {
        if (mManageRoleHolderStateLiveData.getValue() != ManageRoleHolderStateLiveData.STATE_IDLE) {
            Log.i(LOG_TAG, "Trying to set default app while another request is on-going");
            return;
        }
        mManageRoleHolderStateLiveData.setRoleHolderAsUser(mRole.getName(), packageName, true, 0,
                user, getApplication());
    }

    /**
     * Set "None" as the default app.
     */
    public void setNoneDefaultApp() {
        Context context = getApplication();
        UserHandle user = mRole.getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP
                ? UserUtils.getProfileParentOrSelf(mUser, context)
                : mUser;
        mRole.onNoneHolderSelectedAsUser(user, context);
        if (mManageRoleHolderStateLiveData.getValue() != ManageRoleHolderStateLiveData.STATE_IDLE) {
            Log.i(LOG_TAG, "Trying to set default app while another request is on-going");
            return;
        }
        mManageRoleHolderStateLiveData.clearRoleHoldersAsUser(mRole.getName(), 0, user, context);
    }

    /** Sets read screen context enabled for specified application */
    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    public void setReadScreenContextSettingEnabled(RoleApplicationItem holderApplicationItem,
            boolean enabled) {
        int appOpMode = enabled ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED;
        mAppOpsManager.setUidMode(AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                holderApplicationItem.getApplicationInfo().uid, appOpMode);
        if (enabled) {
            mVoiceInteractionManager.clearReadScreenContextRequestDeniedCount();
        }
    }

    @NonNull
    private RoleLiveData createRoleLiveData(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Application application) {
        if (android.permission.flags.Flags.assistSettingsPrivacyImprovementsEnabled()
                && Objects.equals(role.getName(), RoleManager.ROLE_ASSISTANT)) {
            return new AssistantRoleLiveData(role, user, application);
        } else {
            return new RoleLiveData(role, user, application);
        }
    }

    /**
     * {@link ViewModelProvider.Factory} for {@link DefaultAppViewModel}.
     */
    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        private Role mRole;

        @NonNull
        private UserHandle mUser;

        @NonNull
        private Application mApplication;

        public Factory(@NonNull Role role, @NonNull UserHandle user,
                @NonNull Application application) {
            mRole = role;
            mUser = user;
            mApplication = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new DefaultAppViewModel(mRole, mUser, mApplication);
        }
    }
}
