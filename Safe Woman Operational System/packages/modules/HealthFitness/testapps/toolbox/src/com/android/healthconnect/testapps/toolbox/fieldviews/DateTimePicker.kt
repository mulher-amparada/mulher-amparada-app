/**
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *      http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.testapps.toolbox.fieldviews

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.InputType
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import com.android.healthconnect.testapps.toolbox.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@SuppressLint("ViewConstructor")
class DateTimePicker(context: Context, fieldName: String, setPreviousHour: Boolean = false) :
    InputFieldView(context) {

    private var localDateTime =
        LocalDateTime.now(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.HOURS)
            .minusHours(if (setPreviousHour) 1 else 0)

    init {
        inflate(context, R.layout.date_time_picker, this)
        requireViewById<TextView>(R.id.title).text = fieldName
        setupDate()
        setupTime()
    }

    private fun setupDate() {
        requireViewById<EditText>(R.id.select_date).let { date ->
            date.setText(getDateString())
            date.inputType = InputType.TYPE_NULL
            date.setOnClickListener { showDatePicker(date) }
        }
    }

    private fun setupTime() {
        requireViewById<EditText>(R.id.select_time).let { time ->
            time.setText(getTimeString())
            time.inputType = InputType.TYPE_NULL
            time.setOnClickListener { showTimePicker(time) }
        }
    }

    private fun getDateString(): String {
        return localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun getTimeString(): String {
        return localDateTime.toLocalTime().toString()
    }

    private fun showDatePicker(text: EditText) {
        val picker =
            DatePickerDialog(
                context,
                { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    localDateTime =
                        localDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
                    text.setText(getDateString())
                },
                localDateTime.year,
                localDateTime.monthValue - 1,
                localDateTime.dayOfMonth,
            )
        picker.show()
    }

    private fun showTimePicker(text: EditText) {
        val picker =
            TimePickerDialog(
                context,
                { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    localDateTime = localDateTime.withHour(hourOfDay).withMinute(minute)
                    text.setText(getTimeString())
                },
                localDateTime.hour,
                localDateTime.minute,
                true,
            )
        picker.show()
    }

    override fun getFieldValue(): Instant {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant()
    }

    override fun isEmpty(): Boolean {
        return false
    }
}
