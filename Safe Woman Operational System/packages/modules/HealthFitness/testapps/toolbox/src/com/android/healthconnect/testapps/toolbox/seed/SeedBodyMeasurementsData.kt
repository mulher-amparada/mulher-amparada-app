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
import android.health.connect.datatypes.BasalMetabolicRateRecord
import android.health.connect.datatypes.BodyFatRecord
import android.health.connect.datatypes.BodyWaterMassRecord
import android.health.connect.datatypes.BoneMassRecord
import android.health.connect.datatypes.DataOrigin
import android.health.connect.datatypes.Device
import android.health.connect.datatypes.HeightRecord
import android.health.connect.datatypes.LeanBodyMassRecord
import android.health.connect.datatypes.Metadata
import android.health.connect.datatypes.WeightRecord
import android.health.connect.datatypes.units.Length
import android.health.connect.datatypes.units.Mass
import android.health.connect.datatypes.units.Percentage
import android.health.connect.datatypes.units.Power
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.getMetaData
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.insertRecords
import kotlinx.coroutines.runBlocking
import java.time.Duration.ofDays
import java.time.Duration.ofMinutes
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class SeedBodyMeasurementsData(private val context: Context, private val manager: HealthConnectManager)  {

    private val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
    private val yesterday = start.minus(ofDays(1))
    private val lastWeek = start.minus(ofDays(7))
    private val lastMonth = start.minus(ofDays(31))

    fun seedBodyMeasurementsData(){
        runBlocking {
            try {
                seedBodyFatRecord()
                seedBodyWaterMassRecord()
                seedHeightRecord()
                seedBoneMassRecord()
                seedLeanBodyMassRecord()
                seedBasalMetabolicRate()
                seedWeightRecord()

            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    private suspend fun seedBodyFatRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBodyFatRecord(getValidBodyFatPercentage(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBodyFatRecord(getValidBodyFatPercentage(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBodyFatRecord(getValidBodyFatPercentage(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBodyFatRecord(getValidBodyFatPercentage(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBodyWaterMassRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBodyWaterMassRecord(getValidMassData(20000, 45000), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBodyWaterMassRecord(getValidMassData(20000, 45000), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBodyWaterMassRecord(getValidMassData(20000, 45000), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBodyWaterMassRecord(getValidMassData(20000, 45000), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBoneMassRecord(){
        val records = (1L..3).map { timeOffSet ->
            getBoneMassRecord(getValidMassData(5000, 11000), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBoneMassRecord(getValidMassData(5000, 11000), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBoneMassRecord(getValidMassData(5000, 11000), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBoneMassRecord(getValidMassData(5000, 11000), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedHeightRecord(){
        val records = (1L..3).map { timeOffSet ->
            getHeightRecord(getValidHeightData(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getHeightRecord(getValidHeightData(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getHeightRecord(getValidHeightData(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getHeightRecord(getValidHeightData(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedBasalMetabolicRate(){
        val records = (1L..3).map { timeOffSet ->
            getBasalMetabolicRateRecord(getValidBasalMetabolicRateData(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getBasalMetabolicRateRecord(getValidBasalMetabolicRateData(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getBasalMetabolicRateRecord(getValidBasalMetabolicRateData(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getBasalMetabolicRateRecord(getValidBasalMetabolicRateData(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedLeanBodyMassRecord(){
        val records = (1L..3).map { timeOffSet ->
            getLeanBodyMassRecord(getValidMassData(45000, 90000), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getLeanBodyMassRecord(getValidMassData(45000, 90000), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getLeanBodyMassRecord(getValidMassData(45000, 90000), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getLeanBodyMassRecord(getValidMassData(45000, 90000), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedWeightRecord(){
        val records = (1L..3).map { timeOffSet ->
            getWeightRecord(getValidMassData(45000, 90000), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getWeightRecord(getValidMassData(45000, 90000), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getWeightRecord(getValidMassData(45000, 90000), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getWeightRecord(getValidMassData(45000, 90000), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private fun getBodyFatRecord(percentage: Percentage, time: Instant): BodyFatRecord {
        return BodyFatRecord.Builder(getMetaData(context), time, percentage).build()
    }

    private fun getValidBodyFatPercentage(): Percentage {
        return Percentage.fromValue(Random.nextInt(10, 25).toDouble())
    }

    private fun getBodyWaterMassRecord(bodyWaterMass: Mass, time: Instant): BodyWaterMassRecord {
        return BodyWaterMassRecord.Builder(getMetaData(context), time, bodyWaterMass).build()
    }

    private fun getValidMassData(min: Int, max: Int): Mass {
        return Mass.fromGrams(Random.nextInt(min, max).toDouble())
    }

    private fun getBoneMassRecord(mass: Mass, time: Instant): BoneMassRecord {
        return BoneMassRecord.Builder(getMetaData(context), time, mass).build()
    }

    private fun getHeightRecord(height: Length, time: Instant): HeightRecord {
        return HeightRecord.Builder(getMetaData(context), time, height).build()
    }

    private fun getValidHeightData(): Length {
        return Length.fromMeters(Random.nextDouble(1.6, 1.8))
    }

    private fun getLeanBodyMassRecord(mass: Mass, time: Instant): LeanBodyMassRecord {
        return LeanBodyMassRecord.Builder(getMetaData(context), time, mass).build()
    }

    private fun getWeightRecord(weight: Mass, time: Instant): WeightRecord {
        return WeightRecord.Builder(getMetaData(context), time, weight).build()
    }

    private fun getBasalMetabolicRateRecord(basalMetabolicRate: Power, time: Instant): BasalMetabolicRateRecord {
        return BasalMetabolicRateRecord.Builder(getMetaData(context), time, basalMetabolicRate).build()
    }

    private fun getValidBasalMetabolicRateData() : Power {
        return Power.fromWatts(Random.nextInt(65, 80).toDouble())
    }
}