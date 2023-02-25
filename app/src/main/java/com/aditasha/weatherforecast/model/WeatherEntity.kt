package com.aditasha.weatherforecast.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("weather")
data class WeatherEntity(
    @PrimaryKey(false)
    @ColumnInfo("id")
    val id: Int? = null,

    @ColumnInfo("city_id")
    val cityId: Int? = null,

    @ColumnInfo("name")
    val name: String? = null,

    @ColumnInfo("dt")
    val dt: Long? = null,

    @ColumnInfo("icon")
    val icon: String? = null,

    @ColumnInfo("desc")
    val desc: String? = null,

    @ColumnInfo("temp")
    val temp: Double? = null,

    @ColumnInfo("feels")
    val feels: Double? = null,
) : Parcelable