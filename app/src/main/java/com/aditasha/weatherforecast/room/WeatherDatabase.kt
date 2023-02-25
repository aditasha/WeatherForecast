package com.aditasha.weatherforecast.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aditasha.weatherforecast.model.WeatherEntity

@Database(
    entities = [WeatherEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}