package com.aditasha.weatherforecast.repo

import com.aditasha.weatherforecast.model.WeatherEntity
import com.aditasha.weatherforecast.model.WeatherResponse

object DataMapper {

    fun mapWeatherResponseToEntity(input: WeatherResponse, currentLocation: Boolean): WeatherEntity {
        val item = input.weather?.get(0)
        val main = input.main
        val id =
            if (currentLocation) 0
            else {
                when (input.id) {
                        5128638 -> {1} //NY
                        1880252 -> {2} //Singapore
                        1275339 -> {3} //Mumbai
                        1273294 -> {4} //Delhi
                        2147714 -> {5} //Sydney
                        2158177 -> {6} //Melbourne
                        else -> {0}
                    }
                }
        return WeatherEntity(
            id,
            input.id,
            input.name,
//            input.dt?.times(1000),
            System.currentTimeMillis(),
            item?.icon,
            item?.description,
            main?.temp,
            main?.feelsLike,
        )
    }
}