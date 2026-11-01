package com.android.managedprovisioning.common

import android.app.Activity
import com.android.onboarding.nodes.NodeId
import com.android.onboarding.nodes.OnboardingNodeId
import com.android.onboarding.nodes.nodeId
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
interface CommonModule {
    @Binds
    fun bind(impl: DefaultFlags): Flags

    companion object {
        @Provides
        @OnboardingNodeId
        fun nodeId(activity: Activity): NodeId = activity.nodeId
    }
}
