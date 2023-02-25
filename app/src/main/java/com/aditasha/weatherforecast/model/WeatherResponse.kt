package com.aditasha.weatherforecast.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class WeatherResponse(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("main")
    val main: Main? = null,

    @field:SerializedName("dt")
    val dt: Long? = null,

    @field:SerializedName("weather")
    val weather: List<WeatherItem?>? = null,

    @field:SerializedName("name")
    val name: String? = null,
)

@Parcelize
data class WeatherItem(

    @field:SerializedName("icon")
    val icon: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("main")
    val main: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
) : Parcelable

data class Main(

    @field:SerializedName("temp")
    val temp: Double? = null,

    @field:SerializedName("feels_like")
    val feelsLike: Double? = null,
)
