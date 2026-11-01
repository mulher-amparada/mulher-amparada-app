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
import android.health.connect.datatypes.DataOrigin
import android.health.connect.datatypes.Device
import android.health.connect.datatypes.HydrationRecord
import android.health.connect.datatypes.Metadata
import android.health.connect.datatypes.NutritionRecord
import android.health.connect.datatypes.units.Volume
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

class SeedNutritionData(private val context: Context, private val manager: HealthConnectManager) {

    private val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
    private val yesterday = start.minus(ofDays(1))
    private val lastWeek = start.minus(ofDays(7))
    private val lastMonth = start.minus(ofDays(31))

    fun seedNutritionData(){
        runBlocking {
            try {
                seedNutritionRecord()
                seedHydrationRecord()
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    private suspend fun seedNutritionRecord(){
        val records = (1L..3).map { timeOffSet ->
            getNutritionRecord(start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getNutritionRecord(yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getNutritionRecord(lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getNutritionRecord(lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private suspend fun seedHydrationRecord(){
        val records = (1L..3).map { timeOffSet ->
            getHydrationRecord(getValidVolumeData(), start.plus(ofMinutes(timeOffSet)))
        }
        val yesterdayRecords = (1L..3).map { timeOffSet ->
            getHydrationRecord(getValidVolumeData(), yesterday.plus(ofMinutes(timeOffSet)))
        }
        val lastWeekRecords = (1L..3).map { timeOffSet ->
            getHydrationRecord(getValidVolumeData(), lastWeek.plus(ofMinutes(timeOffSet)))
        }
        val lastMonthRecords = (1L..3).map { timeOffSet ->
            getHydrationRecord(getValidVolumeData(), lastMonth.plus(ofMinutes(timeOffSet)))
        }

        insertRecords(records, manager)
        insertRecords(yesterdayRecords, manager)
        insertRecords(lastWeekRecords, manager)
        insertRecords(lastMonthRecords, manager)
    }

    private fun getNutritionRecord(time: Instant): NutritionRecord {
        return NutritionRecord.Builder(getMetaData(context), time, time.plusSeconds(30)).build()
    }

    private fun getHydrationRecord(volume: Volume, time: Instant): HydrationRecord {
        return HydrationRecord.Builder(getMetaData(context), time, time.plusSeconds(30), volume).build()
    }

    private fun getValidVolumeData(): Volume {
        return Volume.fromLiters(Random.nextDouble(0.1, 4.0).toDouble())
    }
}