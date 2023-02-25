package com.aditasha.weatherforecast.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.weatherforecast.model.Result
import com.aditasha.weatherforecast.model.WeatherEntity
import com.aditasha.weatherforecast.repo.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _otherCities = MutableSharedFlow<Result<Any>>()
    val otherCities: SharedFlow<Result<Any>> = _otherCities

    private val _currentLocation = MutableSharedFlow<Result<Any>>()
    val currentLocation: SharedFlow<Result<Any>> = _currentLocation

    private val _weatherResult = MutableSharedFlow<Result<List<WeatherEntity>>>()
    val weatherResult: SharedFlow<Result<List<WeatherEntity>>> = _weatherResult

    fun fetchWeatherOtherCities(cityIds: Array<String>) {
        viewModelScope.launch {
            _otherCities.emitAll(repository.fetchWeatherOtherCities(cityIds))
        }
    }

    fun fetchWeatherCurrentLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _currentLocation.emitAll(repository.fetchWeatherCurrentLocation(lat, lon))
        }
    }

    fun fetchWeatherFromDatabase() {
        viewModelScope.launch {
            _weatherResult.emitAll(repository.fetchWeatherFromDatabase())
        }
    }
}