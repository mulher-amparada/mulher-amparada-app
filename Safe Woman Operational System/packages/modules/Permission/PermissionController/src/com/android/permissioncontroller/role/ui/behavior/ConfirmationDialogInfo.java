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

package com.android.permissioncontroller.role.ui.behavior;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The confirmation dialog info for adding an application as a role holder.
 */
public class ConfirmationDialogInfo implements Parcelable {

    @NonNull
    public static final Creator<ConfirmationDialogInfo> CREATOR =
            new Creator<ConfirmationDialogInfo>() {
                @Override
                public ConfirmationDialogInfo createFromParcel(@NonNull Parcel source) {
                    boolean showIcon = source.readBoolean();
                    CharSequence title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
                    CharSequence message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
                    CharSequence positiveButtonText =
                            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
                    CharSequence negativeButtonText =
                            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
                    boolean isChangeConfirmation = source.readBoolean();
                    return new ConfirmationDialogInfo(showIcon, title, message, positiveButtonText,
                            negativeButtonText, isChangeConfirmation);
                }

                @Override
                public ConfirmationDialogInfo[] newArray(int size) {
                    return new ConfirmationDialogInfo[size];
                }
            };

    /**
     * Whether the confirmation dialog should show the icon of the application.
     */
    private boolean mShowIcon;

    /**
     * The title of the confirmation dialog, or {@code null} if none.
     */
    @Nullable
    private CharSequence mTitle;

    /**
     * The message of the confirmation dialog.
     */
    @NonNull
    private CharSequence mMessage;

    /**
     * The positive button text of the confirmation dialog.
     */
    @NonNull
    private CharSequence mPositiveButtonText;

    /**
     * The negative button text of the confirmation dialog.
     */
    @NonNull
    private CharSequence mNegativeButtonText;

    /**
     * Whether the confirmation dialog is for generally changing a default app, instead of a warning
     * specific to the new default app.
     */
    private boolean mChangeConfirmation;

    /**
     * Create a new instance of this confirmation dialog info.
     *
     * @param showIcon whether the confirmation dialog should show the icon of the application
     * @param title the title of the confirmation dialog, or {@code null} if none
     * @param message the message of the confirmation dialog
     * @param positiveButtonText the positive button text of the confirmation dialog
     * @param negativeButtonText the negative button text of the confirmation dialog
     * @param isChangeConfirmation whether the confirmation is for changing a default app
     *
     * @return a new instance of this confirmation dialog info
     */
    public ConfirmationDialogInfo(boolean showIcon, @Nullable CharSequence title,
            @NonNull CharSequence message, @NonNull CharSequence positiveButtonText,
            @NonNull CharSequence negativeButtonText, boolean isChangeConfirmation) {
        mShowIcon = showIcon;
        mTitle = title;
        mMessage = message;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
        mChangeConfirmation = isChangeConfirmation;
    }

    /**
     * Create a new instance of this confirmation dialog info.
     *
     * @param title the title of the confirmation dialog, or {@code null} if none
     * @param message the message of the confirmation dialog
     * @param positiveButtonText the positive button text of the confirmation dialog
     * @param negativeButtonText the negative button text of the confirmation dialog
     *
     * @return a new instance of this confirmation dialog info
     */
    public ConfirmationDialogInfo(@Nullable CharSequence title, @NonNull CharSequence message,
            @NonNull CharSequence positiveButtonText,
            @NonNull CharSequence negativeButtonText, boolean isChangeConfirmation) {
        this(false, title, message, positiveButtonText, negativeButtonText, isChangeConfirmation);
    }

    /** Returns {@code true} if confirmation dialog should show icon */
    public boolean shouldShowIcon() {
        return mShowIcon;
    }

    @Nullable
    public CharSequence getTitle() {
        return mTitle;
    }

    @NonNull
    public CharSequence getMessage() {
        return mMessage;
    }

    @NonNull
    public CharSequence getPositiveButtonText() {
        return mPositiveButtonText;
    }

    @NonNull
    public CharSequence getNegativeButtonText() {
        return mNegativeButtonText;
    }

    public boolean isChangeConfirmation() {
        return mChangeConfirmation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeBoolean(mShowIcon);
        TextUtils.writeToParcel(mTitle, dest, 0);
        TextUtils.writeToParcel(mMessage, dest, 0);
        TextUtils.writeToParcel(mPositiveButtonText, dest, 0);
        TextUtils.writeToParcel(mNegativeButtonText, dest, 0);
        dest.writeBoolean(mChangeConfirmation);
    }
}
