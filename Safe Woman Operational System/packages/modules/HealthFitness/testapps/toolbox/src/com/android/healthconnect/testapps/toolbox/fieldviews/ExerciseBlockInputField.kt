package com.android.healthconnect.testapps.toolbox.fieldviews

import android.annotation.SuppressLint
import android.content.Context
import android.health.connect.datatypes.ExerciseCompletionGoal
import android.health.connect.datatypes.ExercisePerformanceGoal
import android.health.connect.datatypes.ExerciseSegmentType
import android.health.connect.datatypes.PlannedExerciseBlock
import android.health.connect.datatypes.PlannedExerciseStep
import android.health.connect.datatypes.units.Length
import android.health.connect.datatypes.units.Velocity
import android.widget.CheckBox
import android.widget.LinearLayout
import com.android.healthconnect.testapps.toolbox.R

@SuppressLint("ViewConstructor")
class ExerciseBlockInputField(
    context: Context,
    repetitionsField: EditableTextView,
    descriptionField: EditableTextView,
    generateTestExerciseSteps: CheckBox
) : InputFieldView(context) {
    var repetitionsField: EditableTextView
    var descriptionField: EditableTextView
    var generateTestExerciseSteps: CheckBox

    init {
        inflate(context, R.layout.fragment_exercise_block_input_field, this)
        val containerView = findViewById<LinearLayout>(R.id.exercise_block_input_field)
        this.repetitionsField = repetitionsField
        this.descriptionField = descriptionField
        this.generateTestExerciseSteps = generateTestExerciseSteps
        generateTestExerciseSteps.text =
            context.getString(R.string.training_plan_exercise_block_notice_text)
        containerView.addView(repetitionsField)
        containerView.addView(descriptionField)
        containerView.addView(generateTestExerciseSteps)
    }

    override fun getFieldValue(): Any {
        if (generateTestExerciseSteps.isChecked) {
            return PlannedExerciseBlock.Builder(
                    Integer.parseInt(repetitionsField.getFieldValue().toString()))
                .setDescription(descriptionField.getFieldValue().toString())
                .setSteps(
                    listOf(
                        PlannedExerciseStep.Builder(
                                ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_RUNNING,
                                PlannedExerciseStep.EXERCISE_CATEGORY_ACTIVE,
                                ExerciseCompletionGoal.DistanceGoal(Length.fromMeters(4000.0)))
                            .setDescription("This is a test exercise step")
                            .setPerformanceGoals(
                                listOf(
                                    ExercisePerformanceGoal.HeartRateGoal(150, 180),
                                    ExercisePerformanceGoal.SpeedGoal(
                                        Velocity.fromMetersPerSecond(50.0),
                                        Velocity.fromMetersPerSecond(25.0))))
                            .build()))
                .build()
        } else {
            return PlannedExerciseBlock.Builder(
                    Integer.parseInt(repetitionsField.getFieldValue().toString()))
                .setDescription(descriptionField.getFieldValue().toString())
                .setSteps(listOf())
                .build()
        }
    }

    override fun isEmpty(): Boolean {
        return repetitionsField.isEmpty() || descriptionField.isEmpty()
    }
}
