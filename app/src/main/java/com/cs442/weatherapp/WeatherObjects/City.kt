package com.cs442.weatherapp.WeatherObjects

data class City (
    val name: String,
    val coord: Coordinate,
    val country: String,
    val sunrise: Float,
    val sunset: Float
)

data class Coordinate (
    val lat: Double,
    val lon: Double
)
