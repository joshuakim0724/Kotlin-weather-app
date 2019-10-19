package com.cs442.weatherapp.WeatherObjects

import android.accounts.AuthenticatorDescription
import com.google.gson.annotations.SerializedName

data class Day (
    val dt: Float,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

data class Main (
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)

data class Weather (
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)