package com.cs442.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import com.cs442.weatherapp.WeatherObjects.Day
import com.cs442.weatherapp.WeatherObjects.Forecast
import kotlin.math.round
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var locationGps: Location? = null
    private val API = "7aa19a63280e2cbcae94f299dfc9b749"

    private val INITAL_PERMS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!(PackageManager.PERMISSION_GRANTED==checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))) {
            requestPermissions(INITAL_PERMS, 1337)
        }

        OpenWeatherApi().execute()

        refresh.setOnClickListener {
            OpenWeatherApi().execute()
        }
    }

    // This will add a LocationManager so can get the lat/long of the device later
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (hasGps) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    if (location != null) {
                        locationGps = location
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String?) {}
                override fun onProviderDisabled(provider: String?) {}
            })

            val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (localGpsLocation != null) {
                locationGps = localGpsLocation
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    inner class OpenWeatherApi : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            getLocation()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            var lat = locationGps!!.latitude
            var lon = locationGps!!.longitude

            try {
                response = URL("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&units=metric&appid=$API").readText(Charsets.UTF_8)
            } catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val gsonBuilder = Gson()
                val forecast :Forecast = gsonBuilder.fromJson(result, Forecast::class.java)
                createFrontEnd(forecast)
            } catch(e: Exception) {
                findViewById<TextView>(R.id.address).text = "error"
                throw IllegalArgumentException(e)
            }
        }
    }

//    Could seperate front end parts into different functions but just going to leave comments for now
    private fun createFrontEnd(forecast: Forecast) {
        val parsedList = parseDays(forecast)

        val dayList = forecast.list
        val address = forecast.city.name + ", " + forecast.city.country

        // Main Weather Information
        findViewById<TextView>(R.id.address).setText(address)
        findViewById<TextView>(R.id.weatherDate).setText(parseDate(parsedList[0][0].dt_txt))
        findViewById<TextView>(R.id.mainCurrTemp).setText(round(getAverageTemp(parsedList[0])).toString() + "°C   " + dayList[0].weather[0].main)

        var tempString = round(getTempHigh(parsedList[0])).toString() + "°C/ "
        tempString += round(getTempLow(parsedList[0])).toString() + "°C"
        findViewById<TextView>(R.id.mainTemp).setText(tempString)
        findViewById<TextView>(R.id.mainHumdity).setText(getAverageHumidity(parsedList[0]).toString() + "%" + " Humidity")

        // Daily Information
        findViewById<TextView>(R.id.dayOne).setText(parseDate(parsedList[1][0].dt_txt))
        findViewById<TextView>(R.id.dayTwo).setText(parseDate(parsedList[2][0].dt_txt))
        findViewById<TextView>(R.id.dayThree).setText(parseDate(parsedList[3][0].dt_txt))

        tempString = round(getTempHigh(parsedList[1])).toString() + "°C/ "
        tempString += round(getTempLow(parsedList[1])).toString() + "°C"
        findViewById<TextView>(R.id.dayOneTemp).setText(tempString)

        tempString = round(getTempHigh(parsedList[2])).toString() + "°C/ "
        tempString += round(getTempLow(parsedList[2])).toString() + "°C"
        findViewById<TextView>(R.id.dayTwoTemp).setText(tempString)

        tempString = round(getTempHigh(parsedList[3])).toString() + "°C/ "
        tempString += round(getTempLow(parsedList[3])).toString() + "°C"
        findViewById<TextView>(R.id.dayThreeTemp).setText(tempString)

        findViewById<TextView>(R.id.dayOneHum).setText(getAverageHumidity(parsedList[1]).toString() + "%" + " Humidity")
        findViewById<TextView>(R.id.dayTwoHum).setText(getAverageHumidity(parsedList[2]).toString() + "%" + " Humidity")
        findViewById<TextView>(R.id.dayThreeHum).setText(getAverageHumidity(parsedList[3]).toString() + "%" + " Humidity")

        // Hourly Information
        findViewById<TextView>(R.id.hrOne).setText(parseTime(dayList[1].dt_txt))
        findViewById<TextView>(R.id.hrTwo).setText(parseTime(dayList[2].dt_txt))
        findViewById<TextView>(R.id.hrThree).setText(parseTime(dayList[3].dt_txt))
        findViewById<TextView>(R.id.hrFour).setText(parseTime(dayList[4].dt_txt))
        findViewById<TextView>(R.id.hrFive).setText(parseTime(dayList[5].dt_txt))

        findViewById<TextView>(R.id.hrOneTemp).setText(round(dayList[1].main.temp).toString() + "°C")
        findViewById<TextView>(R.id.hrTwoTemp).setText(round(dayList[2].main.temp).toString() + "°C")
        findViewById<TextView>(R.id.hrThreeTemp).setText(round(dayList[3].main.temp).toString() + "°C")
        findViewById<TextView>(R.id.hrFourTemp).setText(round(dayList[4].main.temp).toString() + "°C")
        findViewById<TextView>(R.id.hrFiveTemp).setText(round(dayList[5].main.temp).toString() + "°C")

        findViewById<TextView>(R.id.hrOneHum).setText(dayList[1].main.humidity.toString() + "%")
        findViewById<TextView>(R.id.hrTwoHum).setText(dayList[2].main.humidity.toString() + "%")
        findViewById<TextView>(R.id.hrThreeHum).setText(dayList[3].main.humidity.toString() + "%")
        findViewById<TextView>(R.id.hrFourHum).setText(dayList[4].main.humidity.toString() + "%")
        findViewById<TextView>(R.id.hrFiveHum).setText(dayList[5].main.humidity.toString() + "%")

        parsePicture(mainImage, dayList[0])

        parsePicture(dayOneImage, parsedList[1][parsedList[1].size/2])
        parsePicture(dayTwoImage, parsedList[2][parsedList[2].size/2])
        parsePicture(dayThreeImage, parsedList[3][parsedList[3].size/2])

        parsePicture(hrOneImage, dayList[1])
        parsePicture(hrTwoImage, dayList[2])
        parsePicture(hrThreeImage, dayList[3])
        parsePicture(hrFourImage, dayList[4])
        parsePicture(hrFiveImage, dayList[5])

    }

    private fun parsePicture(view: ImageView, day: Day) {
        val icon = day.weather[0].icon

        when (icon) {
            "01d" -> view.setBackgroundResource(R.drawable.d01)
            "02d" -> view.setBackgroundResource(R.drawable.d02)
            "03d" -> view.setBackgroundResource(R.drawable.d03)
            "04d" -> view.setBackgroundResource(R.drawable.d04)
            "09d" -> view.setBackgroundResource(R.drawable.d09)
            "10d" -> view.setBackgroundResource(R.drawable.d10)
            "11d" -> view.setBackgroundResource(R.drawable.d11)
            "13d" -> view.setBackgroundResource(R.drawable.d13)
            "50d" -> view.setBackgroundResource(R.drawable.d50)
            "01n" -> view.setBackgroundResource(R.drawable.n01)
            "02n" -> view.setBackgroundResource(R.drawable.n02)
            "03n" -> view.setBackgroundResource(R.drawable.n03)
            "04n" -> view.setBackgroundResource(R.drawable.n04)
            "09n" -> view.setBackgroundResource(R.drawable.n09)
            "10n" -> view.setBackgroundResource(R.drawable.n10)
            "11n" -> view.setBackgroundResource(R.drawable.n11)
            "13n" -> view.setBackgroundResource(R.drawable.n13)
            else -> view.setBackgroundResource(R.drawable.n50)
        }
    }

    private fun parseDate(date: String) : String {
        val splitDate = date.split(" ")[0]
        return splitDate.split("-")[1] + "/" + splitDate.split("-")[2]
    }

    private fun parseTime(date: String) : String {
        val splitDate = date.split(" ")[1]
        return splitDate.split(":")[0] + ":" + splitDate.split(":")[1]
    }

    private fun parseDays(forecast: Forecast) : ArrayList<ArrayList<Day>> {
        var parsedList: ArrayList<ArrayList<Day>> = ArrayList<ArrayList<Day>>()
        val dayList = forecast.list

        var tempList: ArrayList<Day> = ArrayList<Day>()

        var startDate = dayList[0].dt_txt.split(" ")[0]
        var count = 1
        dayList.forEach { day ->
            val currentDate = day.dt_txt.split(" ")[0]
            if (currentDate == startDate) {
                tempList.add(day)
            } else {
                parsedList.add(tempList)
                tempList = ArrayList<Day>()

                startDate = currentDate
                tempList.add(day)
            }

            if (count == dayList.size) {
                parsedList.add(tempList)
            }
            count++
        }
        return parsedList
    }

    private fun getTempHigh(list: ArrayList<Day>) : Double {
        var highestTemp = list[0].main.temp_max
        list.forEach { day ->
            if (day.main.temp_max > highestTemp) {
                highestTemp = day.main.temp_max
            }
        }

        return highestTemp
    }

    private fun getAverageTemp(list: ArrayList<Day>) : Double {
        var averageTemp = 0.0
        var count = 0.0
        list.forEach { day ->
            averageTemp += day.main.temp
            count++
        }

        return averageTemp / count
    }

    private fun getTempLow(list: ArrayList<Day>) : Double {
        var lowestTemp = list[0].main.temp_min
        list.forEach { day ->
            if (day.main.temp_min <  lowestTemp) {
                lowestTemp = day.main.temp_min
            }
        }

        return lowestTemp
    }

    private fun getAverageHumidity(list: ArrayList<Day>) : Int {
        var averageHumidity = 0
        var count = 0
        list.forEach { day ->
            averageHumidity += day.main.humidity
            count++
        }

        return averageHumidity / count
    }
}
