package com.aditasha.weatherforecast.repo

import com.aditasha.weatherforecast.BuildConfig
import com.aditasha.weatherforecast.RetrofitApiService
import com.aditasha.weatherforecast.model.Result
import com.aditasha.weatherforecast.model.WeatherEntity
import com.aditasha.weatherforecast.model.WeatherResponse
import com.aditasha.weatherforecast.room.WeatherDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val retrofitApiService: RetrofitApiService,
    private val weatherDao: WeatherDao
) {
    fun fetchWeatherOtherCities(cityIds: Array<String>): Flow<Result<Any>> = flow {
        try {
            emit(Result.Loading)
            var entities = emptyArray<WeatherEntity>()
            var errorMessage = ""
            for (id in cityIds) {
                val result =
                    retrofitApiService.fetchWeatherCityId(id, BuildConfig.API_KEY, "metric")
                if (result.isSuccessful) {
                    val weatherResponse = result.body() as WeatherResponse
                    entities += DataMapper.mapWeatherResponseToEntity(weatherResponse, false)
                } else {
                    val jsonObj = JSONObject(result.errorBody()!!.charStream().readText())
                    val message = jsonObj.getString("message")
                    errorMessage = message
//                    emit(Result.Error(message))
                }
            }
            if (errorMessage.isNotBlank()) emit(Result.Error(errorMessage))
            else {
                weatherDao.updateOrInsert(entities)
                emit(Result.Success(Any()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }

    fun fetchWeatherCurrentLocation(lat: Double, lon: Double): Flow<Result<Any>> = flow {
        try {
            emit(Result.Loading)
            val result =
                retrofitApiService.fetchWeatherLatLon(lat, lon, BuildConfig.API_KEY, "metric")
            if (result.isSuccessful) {
                val weatherResponse = result.body() as WeatherResponse
                val entity = DataMapper.mapWeatherResponseToEntity(weatherResponse, true)
                weatherDao.deleteCurrentLocation()
                weatherDao.updateOrInsert(arrayOf(entity))
                emit(Result.Success(Any()))
            } else {
                val jsonObj = JSONObject(result.errorBody()!!.charStream().readText())
                val message = jsonObj.getString("message")
                emit(Result.Error(message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }

    fun fetchWeatherFromDatabase(): Flow<Result<List<WeatherEntity>>> = flow {
        try {
            emit(Result.Loading)
            val entities = weatherDao.fetchAllWeather()
            emit(Result.Success(entities))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }
}