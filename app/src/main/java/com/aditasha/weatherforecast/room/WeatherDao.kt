package com.aditasha.weatherforecast.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aditasha.weatherforecast.model.WeatherEntity

@Dao
interface WeatherDao {

    @Upsert(entity = WeatherEntity::class)
    suspend fun updateOrInsert(entities: Array<WeatherEntity>)

    @Query("DELETE FROM weather WHERE id = 0")
    suspend fun deleteCurrentLocation()

    @Query("SELECT * FROM weather")
    suspend fun fetchAllWeather(): List<WeatherEntity>
}