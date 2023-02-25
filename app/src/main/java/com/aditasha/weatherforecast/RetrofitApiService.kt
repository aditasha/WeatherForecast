package com.aditasha.weatherforecast

import com.aditasha.weatherforecast.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitApiService {
    @GET("weather")
    suspend fun fetchWeatherCityId(
        @Query("id") id: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherResponse>

    @GET("weather")
    suspend fun fetchWeatherLatLon(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherResponse>
}