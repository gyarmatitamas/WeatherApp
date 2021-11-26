package com.example.weatherapp

import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.annotation.RequiresApi
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    val CITY: String = "debrecen,hu"
    val API: String = "a124813b59d8538ad2e27d4e4af87944"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTask().execute()
    }

    inner class weatherTask() : AsyncTask<String, Void, String>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View. VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API&lang=hu")
                    .readText(Charsets.UTF_8)
            }
            catch (e: Exception)
            {
                response = null
            }
            return response
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try{
                val jsonObj = JSONObject(result)

                val main  = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updateAt:Long = jsonObj.getLong("dt") + 3600    // UTC + 1:00
                val updateAtText = "Frissítve: "+SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(updateAt*1000))

                val temp = ""+main.getString("temp").toDouble().roundToInt()+"°C"                   // default is .2 double
                val tempMin = "Minimum : "+main.getString("temp_min").toDouble().roundToInt()+"°C"  // default is .2 double
                val tempMax = "Maximum : "+main.getString("temp_max").toDouble().roundToInt()+"°C"  // default is .2 double
                val weatherDescription = weather.getString("description")

                val humidity = main.getString("humidity") + " %"
                val windSpeed = ""+((wind.getString("speed").toDouble()*3.6).roundToInt()) + " km/h"            // default is meter/sec

                val sunrise: Long = sys.getLong("sunrise") + 3600   // UTC + 1:00
                val sunset: Long = sys.getLong("sunset") + 3600     // UTC + 1:00

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updateAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.humidity).text = humidity
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("HH:mm",Locale.FRANCE).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("HH:mm",Locale.FRANCE).format(Date(sunset*1000))

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            }
            catch(e: Exception)
            {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}