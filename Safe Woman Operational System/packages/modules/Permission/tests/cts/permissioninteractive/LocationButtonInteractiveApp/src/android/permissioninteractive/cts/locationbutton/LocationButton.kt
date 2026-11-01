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

package android.permissioninteractive.cts.locationbutton

import android.app.Activity
import android.app.permissionui.LocationButtonClient
import android.app.permissionui.LocationButtonProvider
import android.app.permissionui.LocationButtonProviderFactory
import android.app.permissionui.LocationButtonRequest
import android.app.permissionui.LocationButtonSession
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import java.util.concurrent.Executor

class LocationButton : FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    private val surfaceView: SurfaceView =
        SurfaceView(context).apply { holder.setFormat(PixelFormat.TRANSPARENT) }
    private val provider: LocationButtonProvider =
        requireNotNull(LocationButtonProviderFactory.create(context)) {
            "LocationButtonProvider must not be null."
        }
    private var session: LocationButtonSession? = null
    private val clientExecutor: Executor = context.mainExecutor

    private lateinit var locationButtonRequest: LocationButtonRequest

    fun setRequest(request: LocationButtonRequest) {
        this.locationButtonRequest = request
    }

    private val clientCallback =
        object : LocationButtonClient {
            override fun onPermissionResult(granted: Boolean) {}

            override fun onSessionError(t: Throwable) {
                Log.e(TAG, "Session Error", t)
            }

            override fun onSessionOpened(openedSession: LocationButtonSession) {
                post {
                    session = openedSession
                    surfaceView.setZOrderOnTop(true)
                    surfaceView.setChildSurfacePackage(openedSession.surfacePackage)
                    surfaceView.visibility = VISIBLE
                    contentDescription = LocationButtonActivity.LOCATION_BUTTON_CONTENT_DESCRIPTION
                }
            }
        }

    init {
        surfaceView.visibility = INVISIBLE
        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        surfaceView.post {
            val hostToken =
                windowToken
                    ?: run {
                        Log.e(TAG, "Failed to open session: windowToken is null")
                        return@post
                    }
            provider.openSession(
                findActivity(),
                hostToken,
                display.displayId,
                this.locationButtonRequest,
                clientExecutor,
                clientCallback,
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        session?.close()
        session = null
    }

    private fun findActivity(): Activity {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("LocationButton must be hosted within an Activity")
    }

    companion object {
        private const val TAG = "LocationButton"
    }
}
