package com.aditasha.weatherforecast.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aditasha.weatherforecast.GlideApp
import com.aditasha.weatherforecast.R
import com.aditasha.weatherforecast.databinding.ActivityMainBinding
import com.aditasha.weatherforecast.model.Result
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val listAdapter = WeatherListAdapter()
    private val otherCitiesArray =
        arrayOf("5128638", "1880252", "1275339", "1273294", "2147714", "2158177")

    private var located = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getMyLastLocation()
            } else {
                val snack = Snackbar.make(
                    binding.root,
                    "Allow location permission to see current location weather",
                    Snackbar.LENGTH_LONG
                )
                val params = snack.view.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                snack.view.layoutParams = params
                snack.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        weatherResultListener()
        currentLocationListener()
        otherCitiesListener()

        binding.otherCities.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = listAdapter
        }

        mainActivityViewModel.fetchWeatherFromDatabase()

        binding.swipeRefresh.setOnRefreshListener {
            getMyLastLocation()
            mainActivityViewModel.fetchWeatherOtherCities(otherCitiesArray)
            binding.swipeRefresh.isRefreshing = false
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()
        mainActivityViewModel.fetchWeatherOtherCities(otherCitiesArray)
    }

    private fun weatherResultListener() {
        lifecycleScope.launchWhenStarted {
            mainActivityViewModel.weatherResult.collectLatest {
                binding.loading.isVisible = it is Result.Loading
                if (it is Result.Error) showError(it.localizedMessage)
                else if (it is Result.Success) {
                    val entities = it.data.toMutableList()
                    binding.noDataCurrentLocation.isVisible = entities.isEmpty()
                    binding.noDataOtherCities.isVisible = entities.isEmpty()

                    val current = entities.find { weatherEntity -> weatherEntity.id == 0 }
                    binding.noDataCurrentLocation.isVisible = current == null

                    current?.let { weatherEntity ->
                        binding.currentLocation.apply {
                            location.text = weatherEntity.name
                            val formatted =
                                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(
                                    weatherEntity.dt
                                )
                            timestamp.text = getString(R.string.updated_at, formatted)

                            val color =
                                ContextCompat.getColor(this@MainActivity, R.color.black)
                            val circularProgressDrawable =
                                CircularProgressDrawable(this@MainActivity).apply {
                                    setColorSchemeColors(color)
                                    strokeWidth = 5f
                                    centerRadius = 15f
                                    start()
                                }

                            GlideApp.with(this@MainActivity)
                                .load(getString(R.string.icon_html, weatherEntity.icon))
                                .apply(
                                    RequestOptions().dontTransform()
                                )
                                .placeholder(circularProgressDrawable)
                                .into(icon)

                            weather.text =
                                weatherEntity.desc?.replaceFirstChar { first -> first.uppercase() }
                            temp.text = getString(
                                R.string.celcius_degree,
                                weatherEntity.temp?.toBigDecimal()
                            )
                            feels.text =
                                getString(R.string.feels, weatherEntity.feels?.toBigDecimal())
                        }
                        entities.remove(weatherEntity)
                    }
                    listAdapter.submitList(entities.toList())
                }
            }
        }
    }

    private fun currentLocationListener() {
        lifecycleScope.launchWhenStarted {
            mainActivityViewModel.currentLocation.collectLatest {
                binding.loading.isVisible = it is Result.Loading
                if (it is Result.Error) showError(it.localizedMessage)
                else if (it is Result.Success) mainActivityViewModel.fetchWeatherFromDatabase()
            }
        }
    }

    private fun otherCitiesListener() {
        lifecycleScope.launchWhenStarted {
            mainActivityViewModel.otherCities.collectLatest {
                binding.loading.isVisible = it is Result.Loading
                if (it is Result.Error) showError(it.localizedMessage)
                else if (it is Result.Success) mainActivityViewModel.fetchWeatherFromDatabase()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLocation = location
                if (location != null) {
                    //update current location
                    mainActivityViewModel.fetchWeatherCurrentLocation(
                        location.latitude,
                        location.longitude
                    )
                    located = true
                } else {
                    val locationRequest =
                        LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            TimeUnit.SECONDS.toMillis(1)
                        )
                            .build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(location: LocationResult) {
                            getMyLastLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                    located = false
                    Toast.makeText(
                        this,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showError(message: String) {
        val snack = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        )
        val params =
            snack.view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        snack.setTextMaxLines(10)
        snack.show()
    }
}