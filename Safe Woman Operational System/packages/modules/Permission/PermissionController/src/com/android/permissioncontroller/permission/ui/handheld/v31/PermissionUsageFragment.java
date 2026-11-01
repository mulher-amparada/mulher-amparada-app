/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.permissioncontroller.permission.ui.handheld.v31;

import static com.android.permissioncontroller.Constants.EXTRA_SESSION_ID;
import static com.android.permissioncontroller.Constants.INVALID_SESSION_ID;
import static com.android.permissioncontroller.PermissionControllerStatsLog.PERMISSION_USAGE_FRAGMENT_INTERACTION;
import static com.android.permissioncontroller.PermissionControllerStatsLog.PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SEE_OTHER_PERMISSIONS_CLICKED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SHOW_7DAYS_CLICKED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SHOW_SYSTEM_CLICKED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_ACTIVITY_VIEWED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.write;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.permissioncontroller.R;
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil;
import com.android.permissioncontroller.appfunctions.ui.v37.AgentUsageDetailsActivity;
import com.android.permissioncontroller.appinteraction.domain.model.v31.AgentActivityItem;
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity;
import com.android.permissioncontroller.permission.ui.handheld.SettingsWithLargeHeader;
import com.android.permissioncontroller.permission.ui.viewmodel.v31.PermissionUsageViewModel;
import com.android.permissioncontroller.permission.ui.viewmodel.v31.PermissionUsageViewModelFactory;
import com.android.permissioncontroller.permission.ui.viewmodel.v31.PermissionUsagesUiState;
import com.android.permissioncontroller.permission.utils.KotlinUtils;
import com.android.permissioncontroller.permission.utils.StringUtils;
import com.android.settingslib.widget.SectionButtonPreference;

import kotlin.Unit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** The main page for the privacy dashboard. */
@RequiresApi(Build.VERSION_CODES.S)
public class PermissionUsageFragment extends SettingsWithLargeHeader {
    private static final String LOG_TAG = PermissionUsageFragment.class.getSimpleName();
    private static final Map<String, Integer> PERMISSION_GROUP_ORDER =
            Map.of(
                    Manifest.permission_group.LOCATION, 0,
                    Manifest.permission_group.CAMERA, 1,
                    Manifest.permission_group.MICROPHONE, 2);
    private static final int DEFAULT_ORDER = 3;

    public static final boolean DEBUG = true;

    private static final int PERMISSION_USAGE_INITIAL_EXPANDED_CHILDREN_COUNT =
            PERMISSION_GROUP_ORDER.size();

    /** Map to represent ordering for permission groups in the permissions usage UI. */
    private static final String KEY_SESSION_ID = "_session_id";

    private static final String SESSION_ID_KEY =
            PermissionUsageFragment.class.getName() + KEY_SESSION_ID;

    private static final int MENU_SHOW_7_DAYS_DATA = Menu.FIRST + 4;
    private static final int MENU_SHOW_24_HOURS_DATA = Menu.FIRST + 5;

    private PermissionUsageViewModel mViewModel;

    private boolean mHasSystemApps;
    private MenuItem mShowSystemMenu;
    private MenuItem mHideSystemMenu;
    private MenuItem mShow7DaysDataMenu;
    private MenuItem mShow24HoursDataMenu;
    private SectionButtonPreference mExpandButton;
    private boolean mOtherExpanded;
    private boolean mMenuItemsCreated = false;

    private PermissionUsageGraphicPreference mGraphic;

    /** Unique Id of a request */
    private long mSessionId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSessionId = savedInstanceState.getLong(SESSION_ID_KEY);
        } else {
            mSessionId = getArguments().getLong(EXTRA_SESSION_ID, INVALID_SESSION_ID);
        }

        PermissionUsageViewModelFactory factory = new PermissionUsageViewModelFactory(
                        getActivity().getApplication());
        mViewModel = new ViewModelProvider(this, factory)
                .get(PermissionUsageViewModel.class);

        // Start out with 'other' permissions not expanded.
        mOtherExpanded = false;

        setHasOptionsMenu(true);
        ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        setLoading(true, false);

        mViewModel.getPermissionUsagesUiLiveData().observe(this, this::updateAllUI);
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        PreferenceGroupAdapter adapter =
                (PreferenceGroupAdapter) super.onCreateAdapter(preferenceScreen);

        adapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        onChanged();
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        onChanged();
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        onChanged();
                    }

                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        onChanged();
                    }
                });

        return adapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.permission_usage_title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mShowSystemMenu =
                menu.add(Menu.NONE, MENU_SHOW_SYSTEM, Menu.NONE, R.string.menu_show_system);
        mHideSystemMenu =
                menu.add(Menu.NONE, MENU_HIDE_SYSTEM, Menu.NONE, R.string.menu_hide_system);
        mShow7DaysDataMenu =
                menu.add(
                        Menu.NONE,
                        MENU_SHOW_7_DAYS_DATA,
                        Menu.NONE,
                        R.string.menu_show_7_days_data);
        mShow24HoursDataMenu =
                menu.add(
                        Menu.NONE,
                        MENU_SHOW_24_HOURS_DATA,
                        Menu.NONE,
                        R.string.menu_show_24_hours_data);
        mMenuItemsCreated = true;
        updateShow7DaysToggle(mViewModel.getShow7DaysData());
        updateShowSystemToggle(mViewModel.getShowSystemApps());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                getActivity().finishAfterTransition();
                return true;
            case MENU_SHOW_SYSTEM:
                write(
                        PERMISSION_USAGE_FRAGMENT_INTERACTION,
                        mSessionId,
                        PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SHOW_SYSTEM_CLICKED);
                mViewModel.updateShowSystem(true);
                break;
            case MENU_HIDE_SYSTEM:
                mViewModel.updateShowSystem(false);
                break;
            case MENU_SHOW_7_DAYS_DATA:
                write(
                        PERMISSION_USAGE_FRAGMENT_INTERACTION,
                        mSessionId,
                        PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SHOW_7DAYS_CLICKED);
                mViewModel.updateShow7Days(true);
                break;
            case MENU_SHOW_24_HOURS_DATA:
                mViewModel.updateShow7Days(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getEmptyViewString() {
        return R.string.no_permission_usages;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putLong(SESSION_ID_KEY, mSessionId);
        }
    }

    private void updateShowSystemToggle(boolean showSystem) {
        if (!mMenuItemsCreated) return;

        if (mHasSystemApps) {
            mShowSystemMenu.setVisible(!showSystem);
            mShowSystemMenu.setEnabled(true);

            mHideSystemMenu.setVisible(showSystem);
            mHideSystemMenu.setEnabled(true);
        } else {
            mShowSystemMenu.setVisible(true);
            mShowSystemMenu.setEnabled(false);

            mHideSystemMenu.setVisible(false);
            mHideSystemMenu.setEnabled(false);
        }
    }

    private void updateShow7DaysToggle(boolean show7Days) {
        if (!mMenuItemsCreated) return;

        mShow7DaysDataMenu.setVisible(!show7Days);
        mShow24HoursDataMenu.setVisible(show7Days);
    }

    /** Updates page content and menu items. */
    private void updateAllUI(PermissionUsagesUiState uiData) {
        Log.v(LOG_TAG, "Privacy dashboard data = " + uiData);
        if (getActivity() == null || uiData instanceof PermissionUsagesUiState.Loading) {
            return;
        }

        PermissionUsagesUiState.Success permissionUsagesUiData =
                (PermissionUsagesUiState.Success) uiData;
        Context context = getActivity();

        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            screen = getPreferenceManager().createPreferenceScreen(context);
            setPreferenceScreen(screen);
        }
        screen.removeAll();

        // Create and sort access count entries
        Map<String, Integer> permissionGroupWithUsageCounts =
                permissionUsagesUiData.getPermissionGroupUsageCount();
        List<Map.Entry<String, Integer>> permissionGroupWithUsageCountsEntries =
                new ArrayList(permissionGroupWithUsageCounts.entrySet());
        List<AgentActivityItem> agentActivityItems = permissionUsagesUiData.getAgentUsages();
        permissionGroupWithUsageCountsEntries.sort(Comparator.comparing(
                        (Map.Entry<String, Integer> permissionGroupWithUsageCount) ->
                                PERMISSION_GROUP_ORDER.getOrDefault(
                                        permissionGroupWithUsageCount.getKey(), DEFAULT_ORDER))
                .thenComparing((Map.Entry<String, Integer> permissionGroupWithUsageCount) ->
                        mViewModel.getPermissionGroupLabel(
                                permissionGroupWithUsageCount.getKey())));
        if (AppFunctionsUtil.isPrivacyDashboardAgentActivityEnabled(context)) {
            agentActivityItems.sort(
                    Comparator
                            .comparing((AgentActivityItem agentActivityItem) ->
                                            mViewModel.getAppFunctionAgentLabel(
                                                    context,
                                                    agentActivityItem.getAgentPackageName())
                            )
                            .thenComparingInt(agentActivityItem ->
                                    agentActivityItem.getUserHandle().getIdentifier()
                            ));
        }

        // Calculate showSystem and show7Days states
        boolean containsSystemAppUsages = permissionUsagesUiData.getContainsSystemAppUsage();
        if (mHasSystemApps != containsSystemAppUsages) {
            mHasSystemApps = containsSystemAppUsages;
        }
        boolean show7Days = permissionUsagesUiData.getShow7Days();
        boolean showSystem = permissionUsagesUiData.getShowSystem();
        updateShow7DaysToggle(show7Days);
        updateShowSystemToggle(showSystem);

        // Add the preference category for app permissions
        PreferenceCategory permissionsCategory = new PreferenceCategory(context);
        permissionsCategory.setTitle(R.string.permission_usage_app_permissions_title);
        screen.addPreference(permissionsCategory);
        mGraphic = new PermissionUsageGraphicPreference(context, show7Days);
        permissionsCategory.addPreference(mGraphic);
        mGraphic.setUsages(permissionGroupWithUsageCounts);
        mGraphic.setShowOtherCategory(mOtherExpanded);

        // Add the preference category for agent activity
        PreferenceCategory agentsCategory = null;
        if (AppFunctionsUtil.isPrivacyDashboardAgentActivityEnabled(context)) {
            agentsCategory = new PreferenceCategory(context);
            agentsCategory.setTitle(R.string.permission_usage_agent_activity_title);
            screen.addPreference(agentsCategory);
            write(PRIVACY_DASHBOARD_AGENT_ACTIVITY_VIEWED, mSessionId, show7Days);
        }

        addUiContent(
                context,
                permissionGroupWithUsageCountsEntries,
                agentActivityItems,
                permissionsCategory,
                agentsCategory,
                showSystem,
                show7Days
        );
    }

    /**
     * Add preferences for permission usages.
     *
     * @param agentsCategory The PreferenceCategory for agents. If this is null, it indicates that
     *                       the gating feature flag is disabled and we shouldn't attempt to add
     *                       preferences to this category.
     */
    private void addUiContent(
            Context context,
            List<Map.Entry<String, Integer>> permissionGroupWithUsageCountEntries,
            List<AgentActivityItem> agentActivityItems,
            PreferenceCategory permissionsCategory,
            PreferenceCategory agentsCategory,
            boolean showSystem,
            boolean show7Days
    ) {
        for (int i = 0; i < permissionGroupWithUsageCountEntries.size(); i++) {
            Map.Entry<String, Integer> permissionUsageEntry =
                    permissionGroupWithUsageCountEntries.get(i);
            PermissionUsageControlPreference permissionUsagePreference =
                    new PermissionUsageControlPreference(
                            context,
                            permissionUsageEntry.getKey(),
                            permissionUsageEntry.getValue(),
                            showSystem,
                            mSessionId,
                            show7Days);
            boolean isVisible = mOtherExpanded
                    || i < PERMISSION_USAGE_INITIAL_EXPANDED_CHILDREN_COUNT;
            permissionUsagePreference.setVisible(isVisible);
            permissionsCategory.addPreference(permissionUsagePreference);
        }

        mExpandButton = new SectionButtonPreference(context);
        mExpandButton.setTitle(R.string.perm_usage_adv_info_title);
        mExpandButton.setIcon(R.drawable.ic_arrow_down);
        mExpandButton.setOnClickListener(view -> onExpandButtonClick(permissionsCategory));
        mExpandButton.setVisible(!mOtherExpanded);
        permissionsCategory.addPreference(mExpandButton);

        if (agentsCategory != null) {
            if (agentActivityItems.isEmpty()) {
                Preference emptyAgentsPreference = new Preference(context);
                emptyAgentsPreference.setTitle(R.string.empty_agent_activity_preference_title);
                emptyAgentsPreference.setSelectable(false);
                agentsCategory.addPreference(emptyAgentsPreference);
            } else {
                for (int i = 0; i < agentActivityItems.size(); i++) {
                    AgentActivityItem agentActivityItem = agentActivityItems.get(i);
                    String agentPackageName = agentActivityItem.getAgentPackageName();
                    int accessCount = show7Days ? agentActivityItem.getAccessCount7Days() :
                            agentActivityItem.getAccessCount24Hours();
                    UserHandle user = agentActivityItem.getUserHandle();
                    Preference agentUsagePreference = new Preference(context);
                    agentUsagePreference.setIcon(
                            KotlinUtils.INSTANCE.getBadgedPackageIcon(
                                    getActivity().getApplication(),
                                    agentPackageName,
                                    user
                            )
                    );
                    agentUsagePreference.setTitle(
                            KotlinUtils.INSTANCE.getPackageLabel(
                                    getActivity().getApplication(),
                                    agentPackageName,
                                    user
                            )
                    );
                    if (accessCount > 0) {
                        agentUsagePreference.setSummary(StringUtils.getIcuPluralsString(
                                context,
                                R.string.agent_usage_preference_label,
                                accessCount
                        ));
                    } else if (show7Days) {
                        agentUsagePreference.setSummary(
                                R.string.agent_activity_preference_summary_no_accesses_7d
                        );
                    } else {
                        agentUsagePreference.setSummary(
                                R.string.agent_activity_preference_summary_no_accesses_24h
                        );
                    }
                    agentUsagePreference.setOnPreferenceClickListener(preference -> {
                        Intent intent = new Intent(context, AgentUsageDetailsActivity.class);
                        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, agentPackageName);
                        intent.putExtra(Intent.EXTRA_USER, user);
                        intent.putExtra(ManagePermissionsActivity.EXTRA_SHOW_7_DAYS, show7Days);
                        intent.putExtra(ManagePermissionsActivity.EXTRA_SHOW_SYSTEM, showSystem);
                        context.startActivity(intent);
                        return true;
                    });
                    agentsCategory.addPreference(agentUsagePreference);
                }
            }
        }

        setLoading(false, true);
    }

    private Unit onExpandButtonClick(PreferenceCategory permissionsCategory) {
        mOtherExpanded = true;
        if (mGraphic != null) {
            mGraphic.setShowOtherCategory(true);
        }
        for (int i = 0; i < permissionsCategory.getPreferenceCount(); i++) {
            // On ExpandButton click, just set all permission preferences as visible
            permissionsCategory.getPreference(i).setVisible(true);
        }
        mExpandButton.setVisible(false);

        write(
                PERMISSION_USAGE_FRAGMENT_INTERACTION,
                mSessionId,
                PERMISSION_USAGE_FRAGMENT_INTERACTION__ACTION__SEE_OTHER_PERMISSIONS_CLICKED);
        return Unit.INSTANCE;
    }
}
