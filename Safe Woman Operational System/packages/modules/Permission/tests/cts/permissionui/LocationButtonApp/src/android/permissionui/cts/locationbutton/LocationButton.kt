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

package android.permissionui.cts.locationbutton

import android.app.Activity
import android.app.permissionui.LocationButtonClient
import android.app.permissionui.LocationButtonProvider
import android.app.permissionui.LocationButtonProviderFactory
import android.app.permissionui.LocationButtonRequest
import android.app.permissionui.LocationButtonSession
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import java.util.concurrent.Executor

class LocationButton
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    private val surfaceView: SurfaceView =
        SurfaceView(context).apply {
            visibility = INVISIBLE
            holder.setFormat(PixelFormat.TRANSPARENT)
        }
    private val provider: LocationButtonProvider? = LocationButtonProviderFactory.create(context)
    private var session: LocationButtonSession? = null
    private val clientExecutor: Executor = context.mainExecutor

    var textType: Int? = null

    private val clientCallback =
        object : LocationButtonClient {
            override fun onPermissionResult(granted: Boolean) {
                post { Log.i(TAG, "onPermissionResult granted: $granted") }
            }

            override fun onSessionError(t: Throwable) {
                Log.e(TAG, "Session Error", t)
            }

            override fun onSessionOpened(openedSession: LocationButtonSession) {
                post {
                    session = openedSession
                    surfaceView.setZOrderOnTop(true)
                    surfaceView.setChildSurfacePackage(openedSession.surfacePackage)
                    surfaceView.visibility = VISIBLE
                }
            }
        }

    init {
        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        surfaceView.measure(
            MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY),
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        surfaceView.layout(0, 0, right - left, bottom - top)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        surfaceView.post {
            val hostToken: IBinder = windowToken ?: return@post
            val density = resources.displayMetrics.density
            val request =
                LocationButtonRequest.Builder(width, height, resources.configuration)
                    .setPaddingLeft(paddingLeft)
                    .setPaddingTop(paddingTop)
                    .setPaddingRight(paddingRight)
                    .setPaddingBottom(paddingBottom)
                    .setBackgroundColor(0xFFFFFFFF.toInt())
                    .setTextColor(0xFF000000.toInt())
                    .setIconTint(0xFF000000.toInt())
                    .setStrokeColor(0xFF000000.toInt())
                    .setStrokeWidth((1 * density).toInt())
                    .setCornerRadius(4 * density)
                    .setPressedCornerRadius(20 * density)
                    .setTextType(textType ?: LocationButtonSession.TEXT_TYPE_PRECISE_LOCATION)
                    .build()

            provider?.openSession(
                findActivity(),
                hostToken,
                display.displayId,
                request,
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
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        throw IllegalStateException("LocationButton must be hosted within an Activity context")
    }

    companion object {
        private const val TAG = "LocationButton"
    }
}
