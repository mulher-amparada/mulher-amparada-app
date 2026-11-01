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

package com.android.permissioncontroller.permission.ui.handheld.v37

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.RemoteCallback
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.android.permissioncontroller.Constants
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.ui.model.v37.LocationButtonViewModel
import com.android.permissioncontroller.permission.ui.model.v37.LocationButtonViewModelFactory

/** Fragment to show location button consent dialog on handheld devices. */
@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
class RequestLocationButtonPermissionsFragment : DialogFragment() {
    private lateinit var viewModel: LocationButtonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        val sessionId = args.getLong(Constants.EXTRA_SESSION_ID)
        val packageName = args.getString(Intent.EXTRA_PACKAGE_NAME)!!
        val remoteCallback =
            args.getParcelable(Intent.EXTRA_REMOTE_CALLBACK, RemoteCallback::class.java)!!

        val factory =
            LocationButtonViewModelFactory(
                requireActivity().application,
                sessionId,
                packageName,
                remoteCallback,
            )
        viewModel =
            ViewModelProvider(requireActivity(), factory)[LocationButtonViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val view =
            LayoutInflater.from(activity)
                .inflate(R.layout.request_location_button_permissions_dialog, null)
                .apply {
                    // Location (pin) icon
                    val iconView = requireViewById<ImageView>(R.id.icon)
                    iconView.setImageDrawable(viewModel.locationPinIcon)

                    // Title message
                    val messageView = requireViewById<TextView>(R.id.title)
                    val escapedAppLabel = Html.escapeHtml(viewModel.appLabel)
                    val label =
                        Html.fromHtml(
                            resources.getString(
                                R.string.request_location_button_permissions_dialog_title,
                                escapedAppLabel,
                            ),
                            Html.FROM_HTML_MODE_COMPACT,
                        )
                    messageView.text = label

                    val lottieDrawable = getLottieDrawableForFineLocation()
                    val fineRadioButton =
                        requireViewById<RadioButton>(R.id.precise_location_illustration)
                    fineRadioButton.isChecked = true
                    fineRadioButton.background = getLayerDrawableForLocationAccuracy(lottieDrawable)
                    lottieDrawable.start()

                    // Allow & Deny buttons
                    requireViewById<Button>(R.id.allow).setOnClickListener {
                        viewModel.onAllow()
                        activity.finish()
                    }
                    requireViewById<Button>(R.id.dont_allow).setOnClickListener {
                        viewModel.onDontAllow()
                        activity.finish()
                    }
                }
        return Dialog(activity).apply { setContentView(view) }
    }

    private fun getLayerDrawableForLocationAccuracy(
        locationAccuracyDrawable: LottieDrawable
    ): LayerDrawable {
        val radioButtonBackground =
            activity!!.getDrawable(R.drawable.location_permission_granularity_card_background)

        return LayerDrawable(arrayOf(radioButtonBackground, locationAccuracyDrawable)).apply {
            paddingMode = LayerDrawable.PADDING_MODE_STACK
            val padding =
                activity!!
                    .resources
                    .getDimensionPixelSize(
                        R.dimen.location_permission_grant_dialog_radio_button_padding
                    )
            setLayerInset(1, padding, padding, padding, padding)
            setLayerGravity(1, Gravity.BOTTOM)
        }
    }

    private fun getLottieDrawableForFineLocation(): LottieDrawable {
        val composition =
            LottieCompositionFactory.fromRawResSync(activity, R.raw.fine_loc_radio_on, null).value!!
        val drawable = LottieDrawable()
        drawable.composition = composition
        return drawable
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.onCancel()
        requireActivity().finish()
    }

    companion object {
        fun newInstance(
            sessionId: Long,
            packageName: String,
            remoteCallback: RemoteCallback,
        ): RequestLocationButtonPermissionsFragment =
            RequestLocationButtonPermissionsFragment().apply {
                arguments =
                    Bundle().apply {
                        putLong(Constants.EXTRA_SESSION_ID, sessionId)
                        putString(Intent.EXTRA_PACKAGE_NAME, packageName)
                        putParcelable(Intent.EXTRA_REMOTE_CALLBACK, remoteCallback)
                    }
            }
    }
}
