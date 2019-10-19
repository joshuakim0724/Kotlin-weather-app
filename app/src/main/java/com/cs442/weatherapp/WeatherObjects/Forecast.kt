package com.cs442.weatherapp.WeatherObjects

import com.cs442.weatherapp.WeatherObjects.Day

//Weather information
//○ City, temperature, humidity, conditions
//(thunderstorm, drizzle, rain, snow, clear, clouds, etc.)
//○ Current forecast
//■ Show minimum & maximum temperature of the current day,
//from the data that is visible from the API
//(if API shows data for only 18:00~24:00, get min/max from it)
//
//○ Hourly forecast for the next 15 hours (interval of 3 hours)
//■ Use “temp” for the temperature
//○ Daily forecast for the next 3 days
//■ Calculate the minimum & maximum from the
//temp_min & temp_max of 3 hour interval data of each day
//
//● Refresh Function
//○ E.g. Button, Auto-refresh
//○ Refreshing data takes time - consider using AsyncTask


class Forecast (
    val cnt: Int,
    val list: List<Day>,
    val city: City
)