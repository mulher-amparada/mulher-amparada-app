/**
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.healthconnect.testapps.toolbox.seed

import android.content.Context
import android.health.connect.HealthConnectManager
import android.health.connect.datatypes.BasalBodyTemperatureRecord
import android.health.connect.datatypes.BloodGlucoseRecord
import android.health.connect.datatypes.BloodGlucoseRecord.RelationToMealType
import android.health.connect.datatypes.BloodGlucoseRecord.SpecimenSource
import android.health.connect.datatypes.BloodPressureRecord
import android.health.connect.datatypes.BloodPressureRecord.BloodPressureMeasurementLocation
import android.health.connect.datatypes.BloodPressureRecord.BodyPosition
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_EAR
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FINGER
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FOREHEAD
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_MOUTH
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_RECTUM
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TEMPORAL_ARTERY
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TOE
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_UNKNOWN
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_VAGINA
import android.health.connect.datatypes.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_WRIST
import android.health.connect.datatypes.BodyTemperatureRecord
import android.health.connect.datatypes.HeartRateRecord
import android.health.connect.datatypes.HeartRateVariabilityRmssdRecord
import android.health.connect.datatypes.MealType
import android.health.connect.datatypes.OxygenSaturationRecord
import android.health.connect.datatypes.RespiratoryRateRecord
import android.health.connect.datatypes.RestingHeartRateRecord
import android.health.connect.datatypes.SkinTemperatureRecord
import android.health.connect.datatypes.units.BloodGlucose
import android.health.connect.datatypes.units.Percentage
import android.health.connect.datatypes.units.Pressure
import android.health.connect.datatypes.units.Temperature
import android.health.connect.datatypes.units.TemperatureDelta
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.getMetaData
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.insertRecords
import kotlinx.coroutines.runBlocking
import java.time.Duration.ofDays
import java.time.Duration.ofMinutes
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random
import kotlin.random.Random as ktRandom

class SeedVitalsData(private val context: Context, private val manager: HealthConnectManager) {

    companion object {
        val VALID_SKIN_TEMP_READING_LOCATIONS =
            setOf(
                SkinTemperatureRecord.MEASUREMENT_LOCATION_FINGER,
                SkinTemperatureRecord.MEASUREMENT_LOCATION_TOE,
                SkinTemperatureRecord.MEASUREMENT_LOCATION_WRIST,
                SkinTemperatureRecord.MEASUREMENT_LOCATION_UNKNOWN)
        val VALID_BODY_TEMPERATURE_MEASUREMENT_LOCATIONS =
            setOf(
                MEASUREMENT_LOCATION_EAR,
                MEASUREMENT_LOCATION_FINGER,
                MEASUREMENT_LOCATION_FOREHEAD,
                MEASUREMENT_LOCATION_MOUTH,
                MEASUREMENT_LOCATION_RECTUM,
                MEASUREMENT_LOCATION_TEMPORAL_ARTERY,
                MEASUREMENT_LOCATION_TOE,
                MEASUREMENT_LOCATION_UNKNOWN,
                MEASUREMENT_LOCATION_VAGINA,
                MEASUREMENT_LOCATION_WRIST
            )
        val VALID_SPECIMEN_SOURCE =
            setOf(
                SpecimenSource.SPECIMEN_SOURCE_TEARS,
                SpecimenSource.SPECIMEN_SOURCE_PLASMA,
                SpecimenSource.SPECIMEN_SOURCE_UNKNOWN,
                SpecimenSource.SPECIMEN_SOURCE_WHOLE_BLOOD,
                SpecimenSource.SPECIMEN_SOURCE_SERUM,
                SpecimenSource.SPECIMEN_SOURCE_INTERSTITIAL_FLUID,
                SpecimenSource.SPECIMEN_SOURCE_CAPILLARY_BLOOD
            )
        val VALID_RELATION_TO_MEAL_TYPE =
            setOf(
                RelationToMealType.RELATION_TO_MEAL_AFTER_MEAL,
                RelationToMealType.RELATION_TO_MEAL_BEFORE_MEAL,
                RelationToMealType.RELATION_TO_MEAL_GENERAL,
                RelationToMealType.RELATION_TO_MEAL_FASTING,
                RelationToMealType.RELATION_TO_MEAL_UNKNOWN
            )
        val VALID_MEAL_TYPE =
            setOf(
                MealType.MEAL_TYPE_BREAKFAST,
                MealType.MEAL_TYPE_LUNCH,
                MealType.MEAL_TYPE_SNACK,
                MealType.MEAL_TYPE_DINNER,
                MealType.MEAL_TYPE_UNKNOWN
            )
        val VALID_BLOOD_PRESSURE_MEASUREMENT_LOCATION =
            setOf(
                BloodPressureMeasurementLocation.BLOOD_PRESSURE_MEASUREMENT_LOCATION_LEFT_WRIST,
                BloodPressureMeasurementLocation.BLOOD_PRESSURE_MEASUREMENT_LOCATION_RIGHT_WRIST,
                BloodPressureMeasurementLocation.BLOOD_PRESSURE_MEASUREMENT_LOCATION_LEFT_UPPER_ARM,
                BloodPressureMeasurementLocation.BLOOD_PRESSURE_MEASUREMENT_LOCATION_RIGHT_UPPER_ARM,
                BloodPressureMeasurementLocation.BLOOD_PRESSURE_MEASUREMENT_LOCATION_UNKNOWN
            )
        val VALID_BODY_POSITION =
            setOf(
                BodyPosition.BODY_POSITION_UNKNOWN,
                BodyPosition.BODY_POSITION_STANDING_UP,
                BodyPosition.BODY_POSITION_RECLINING,
                BodyPosition.BODY_POSITION_LYING_DOWN,
                BodyPosition.BODY_POSITION_SITTING_DOWN
            )
    }

    private val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
    private val yesterday = start.minus(ofDays(1))
    private val lastWeek = start.minus(ofDays(7))
    private val lastMonth = start.minus(ofDays(31))

    fun seedVitalsData(){
        runBlocking {
            try {
                seedHeartRateData()
                seedSkinTemperatureData()
                seedBasalBodyTemperatureRecord()
                seedBloodGlucoseRecord()
                seedBloodPressureRecord()
                seedBodyTemperatureRecord()
                seedHeartRateVariabilityRmssdRecord()
                seedOxygenSaturationRecord()
                seedRespiratoryRecord()
                seedRestingHeartRateRecord()
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    private suspend fun seedHeartRateData() {
        val random = Random()
        val records =
            (1L..10).map { timeOffset ->
                val hrSamples = ArrayList<Pair<Long, Instant>>()
                repeat(10) { i ->
                    hrSamples.add(
                        Pair(getValidHeartRate(random), start.plus(ofMinutes(timeOffset + i))))
                }
                getHeartRateRecord(
                    hrSamples,
                    start.plus(ofMinutes(timeOffset)),
                    start.plus(ofMinutes(timeOffset + 100)))
            }

        val yesterdayRecords =
            (1L..3).map { timeOffset ->
                val hrSamples = ArrayList<Pair<Long, Instant>>()
                repeat(10) { i ->
                    hrSamples.add(
                        Pair(getValidHeartRate(random), yesterday.plus(ofMinutes(timeOffset + i))))
                }
                getHeartRateRecord(
                    hrSamples,
                    yesterday.plus(ofMinutes(timeOffset)),
                    yesterday.plus(ofMinutes(timeOffset + 100)))
            }

        val lastWeekRecords =
            (1L..3).map { timeOffset ->
                val hrSamples = ArrayList<Pair<Long, Instant>>()
                repeat(10) { i ->
                    hrSamples.add(
                        Pair(getValidHeartRate(random), lastWeek.plus(ofMinutes(timeOffset + i))))
                }
                getHeartRateRecord(
                    hrSamples,
                    lastWeek.plus(ofMinutes(timeOffset)),
                    start.plus(ofMinutes(timeOffset + 100)))
            }

        val lastMonthRecords =
            (1L..3).map { timeOffset ->
                val hrSamples = ArrayList<Pair<Long, Instant>>()
                repeat(10) { i ->
                    hrSamples.add(
                        Pair(getValidHeartRate(random), lastMonth.plus(ofMinutes(timeOffset + i))))
                }
                getHeartRateRecord(
                    hrSamples,
                    lastMonth.plus(ofMinutes(timeOffset)),
                    lastMonth.plus(ofMinutes(timeOffset + 100)))
            }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedSkinTemperatureData() {
        val records =
            (1L..10).map { timeOffset ->
                val skinTempSamples = ArrayList<SkinTemperatureRecord.Delta>()
                repeat(10) { i ->
                    skinTempSamples.add(
                        SkinTemperatureRecord.Delta(
                            getValidTemperatureDelta(), start.plus(ofMinutes(timeOffset + i))))
                }
                getSkinTemperatureRecord(
                    skinTempSamples,
                    start.plus(ofMinutes(timeOffset)),
                    start.plus(ofMinutes(timeOffset + 100)))
            }
        val yesterdayRecords =
            (1L..3).map { timeOffset ->
                val skinTempSamples = ArrayList<SkinTemperatureRecord.Delta>()
                repeat(10) { i ->
                    skinTempSamples.add(
                        SkinTemperatureRecord.Delta(
                            getValidTemperatureDelta(), yesterday.plus(ofMinutes(timeOffset + i))))
                }
                getSkinTemperatureRecord(
                    skinTempSamples,
                    yesterday.plus(ofMinutes(timeOffset)),
                    yesterday.plus(ofMinutes(timeOffset + 100)))
            }
        val lastWeekRecords =
            (1L..3).map { timeOffset ->
                val skinTempSamples = ArrayList<SkinTemperatureRecord.Delta>()
                repeat(10) { i ->
                    skinTempSamples.add(
                        SkinTemperatureRecord.Delta(
                            getValidTemperatureDelta(), lastWeek.plus(ofMinutes(timeOffset + i))))
                }
                getSkinTemperatureRecord(
                    skinTempSamples,
                    lastWeek.plus(ofMinutes(timeOffset)),
                    lastWeek.plus(ofMinutes(timeOffset + 100)))
            }
        val lastMonthRecords =
            (1L..3).map { timeOffset ->
                val skinTempSamples = ArrayList<SkinTemperatureRecord.Delta>()
                repeat(10) { i ->
                    skinTempSamples.add(
                        SkinTemperatureRecord.Delta(
                            getValidTemperatureDelta(), lastMonth.plus(ofMinutes(timeOffset + i))))
                }
                getSkinTemperatureRecord(
                    skinTempSamples,
                    lastMonth.plus(ofMinutes(timeOffset)),
                    lastMonth.plus(ofMinutes(timeOffset + 100)))
            }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBasalBodyTemperatureRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBasalBodyTemperatureRecord(getValidBodyTemperature(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBasalBodyTemperatureRecord(getValidBodyTemperature(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBasalBodyTemperatureRecord(getValidBodyTemperature(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBasalBodyTemperatureRecord(getValidBodyTemperature(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBloodGlucoseRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBloodGlucoseRecord(
                getValidBloodGlucoseLevel(),
                start.plus(ofMinutes(timeOffSet))
            )
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBloodGlucoseRecord(

                getValidBloodGlucoseLevel(),


                yesterday.plus(ofMinutes(timeOffSet))
            )
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBloodGlucoseRecord(
                getValidBloodGlucoseLevel(),
                lastWeek.plus(ofMinutes(timeOffSet))
            )
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBloodGlucoseRecord(
                getValidBloodGlucoseLevel(),
                lastMonth.plus(ofMinutes(timeOffSet))
            )
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBloodPressureRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBloodPressureRecord(
                getValidSystolicData(),
                getValidDiastolicData(),
                start.plus(ofMinutes(timeOffSet))
            )
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBloodPressureRecord(
                getValidSystolicData(),
                getValidDiastolicData(),
                yesterday.plus(ofMinutes(timeOffSet))
            )
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBloodPressureRecord(
                getValidSystolicData(),
                getValidDiastolicData(),
                lastWeek.plus(ofMinutes(timeOffSet))
            )
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBloodPressureRecord(
                getValidSystolicData(),
                getValidDiastolicData(),
                lastMonth.plus(ofMinutes(timeOffSet))
            )
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBodyTemperatureRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBodyTemperatureRecord(getValidBodyTemperature(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBodyTemperatureRecord(getValidBodyTemperature(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBodyTemperatureRecord(getValidBodyTemperature(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBodyTemperatureRecord(getValidBodyTemperature(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedHeartRateVariabilityRmssdRecord(){
        val records = (1L..3).map { timeOffSet ->
            getHeartRateVariabilityRmssdRecord(getValidRateData(20, 80), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getHeartRateVariabilityRmssdRecord(getValidRateData(20, 80), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getHeartRateVariabilityRmssdRecord(getValidRateData(20, 80), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getHeartRateVariabilityRmssdRecord(getValidRateData(20, 80), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedOxygenSaturationRecord(){
        val records = (1L..3).map { timeOffSet ->
            getOxygenSaturationRecord(getValidPercentageData(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getOxygenSaturationRecord(getValidPercentageData(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getOxygenSaturationRecord(getValidPercentageData(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getOxygenSaturationRecord(getValidPercentageData(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedRespiratoryRecord(){
        val records = (1L..3).map { timeOffSet ->
            getRespiratoryRateRecord(getValidRateData(10, 30), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getRespiratoryRateRecord(getValidRateData(10, 30), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getRespiratoryRateRecord(getValidRateData(10, 30), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getRespiratoryRateRecord(getValidRateData(10, 30), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedRestingHeartRateRecord(){
        val random = Random()
        val records = (1L..3).map { timeOffSet ->
            getRestingHeartRateRecord(getValidHeartRate(random), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getRestingHeartRateRecord(getValidHeartRate(random), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getRestingHeartRateRecord(getValidHeartRate(random), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getRestingHeartRateRecord(getValidHeartRate(random), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private fun getSkinTemperatureRecord(
        deltasList: List<SkinTemperatureRecord.Delta>,
        startTime: Instant,
        endTime: Instant
    ): SkinTemperatureRecord {
        return SkinTemperatureRecord.Builder(getMetaData(context), startTime, endTime)
            .setDeltas(deltasList)
            .setBaseline(Temperature.fromCelsius(25.0))
            .setMeasurementLocation(VALID_SKIN_TEMP_READING_LOCATIONS.random())
            .build()
    }

    private fun getValidTemperatureDelta(): TemperatureDelta {
        return TemperatureDelta.fromCelsius((ktRandom.nextInt(-30, 30)).toDouble() / 10)
    }

    private fun getHeartRateRecord(
        heartRateValues: List<Pair<Long, Instant>>,
        start: Instant,
        end: Instant,
    ): HeartRateRecord {
        return HeartRateRecord.Builder(
            getMetaData(context),
            start,
            end,
            heartRateValues.map { HeartRateRecord.HeartRateSample(it.first, it.second) })
            .build()
    }

    private fun getValidHeartRate(random: Random): Long {
        return (random.nextInt(20) + 80).toLong()
    }

    private fun getBasalBodyTemperatureRecord(bodyTemperature: Temperature, time: Instant): BasalBodyTemperatureRecord {
        return BasalBodyTemperatureRecord.Builder(
            getMetaData(context),
            time,
            VALID_BODY_TEMPERATURE_MEASUREMENT_LOCATIONS.random(),
            bodyTemperature
        ).build()
    }

    private fun getValidBodyTemperature(): Temperature {
        return Temperature.fromCelsius(ktRandom.nextInt(30, 40).toDouble())
    }

    private fun getBloodGlucoseRecord(level: BloodGlucose, time: Instant): BloodGlucoseRecord {
        return BloodGlucoseRecord.Builder(
            getMetaData(context),
            time,
            VALID_SPECIMEN_SOURCE.random(),
            level,
            VALID_RELATION_TO_MEAL_TYPE.random(),
            VALID_MEAL_TYPE.random()
        ).build()
    }

    private fun getValidBloodGlucoseLevel(): BloodGlucose {
        return BloodGlucose.fromMillimolesPerLiter(ktRandom.nextDouble(3.5, 7.5))
    }

    private fun getBloodPressureRecord(systolic: Pressure, diastolic: Pressure, time: Instant): BloodPressureRecord {
        return BloodPressureRecord.Builder(
            getMetaData(context),
            time,
            VALID_BLOOD_PRESSURE_MEASUREMENT_LOCATION.random(),
            systolic,
            diastolic,
            VALID_BODY_POSITION.random()
        ).build()
    }

    private fun getValidSystolicData(): Pressure {
        return Pressure.fromMillimetersOfMercury(ktRandom.nextInt(100, 150).toDouble())
    }

    private fun getValidDiastolicData(): Pressure {
        return Pressure.fromMillimetersOfMercury(ktRandom.nextInt(50, 80).toDouble())
    }

    private fun getBodyTemperatureRecord(bodyTemperature: Temperature, time: Instant): BodyTemperatureRecord {
        return BodyTemperatureRecord.Builder(getMetaData(context), time, VALID_BODY_TEMPERATURE_MEASUREMENT_LOCATIONS.random(), bodyTemperature).build()
    }

    private fun getHeartRateVariabilityRmssdRecord(heartRateVariability: Double, time: Instant): HeartRateVariabilityRmssdRecord {
        return HeartRateVariabilityRmssdRecord.Builder(getMetaData(context), time, heartRateVariability).build()
    }


    private fun getOxygenSaturationRecord(percentage: Percentage, time: Instant): OxygenSaturationRecord {
        return OxygenSaturationRecord.Builder(getMetaData(context), time, percentage).build()
    }

    private fun getValidPercentageData(): Percentage {
        return Percentage.fromValue(ktRandom.nextInt(90, 100).toDouble())
    }

    private fun getRespiratoryRateRecord(rate: Double, time: Instant): RespiratoryRateRecord {
        return RespiratoryRateRecord.Builder(getMetaData(context), time, rate).build()
    }

    private fun getValidRateData(min: Int, max: Int): Double{
        return ktRandom.nextInt(min, max).toDouble()
    }

    private fun getRestingHeartRateRecord(beatsPerMinute: Long, time: Instant): RestingHeartRateRecord {
        return RestingHeartRateRecord.Builder(getMetaData(context), time, beatsPerMinute).build()
    }
}