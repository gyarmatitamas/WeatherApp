package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.se.omapi.Reader
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.*
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import android.widget.AdapterView

class MainActivity : AppCompatActivity() {
    var CITY: String = "debrecen,hu"
    val API: String = "a124813b59d8538ad2e27d4e4af87944"

    var now : String = ""
    var sunsetTime : String = ""
    var sunriseTime : String = ""

    var jsonString : String = ""
    var cities = ListCityModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        jsonString = loadJson(this)
        cities = Gson().fromJson(jsonString, ListCityModel::class.java)
        Log.d("MainActivity","Size: ${cities.data.size}")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN        // hides the status bar (notification bar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherTask().execute()
    }

    inner class weatherTask() : AsyncTask<String, Void, String>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API&lang=hu")
                    .readText(Charsets.UTF_8)
            }
            catch (e: Exception) {
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

                val temp = ""+main.getDouble("temp").roundToInt()+"°C"                   // default is .2 double
                val tempMin = "Minimum : "+main.getDouble("temp_min").roundToInt()+"°C"  // default is .2 double
                val tempMax = "Maximum : "+main.getDouble("temp_max").roundToInt()+"°C"  // default is .2 double
                val weatherDescription = weather.getString("description")

                val humidity = main.getString("humidity") + " %"
                val windSpeed = ""+((wind.getDouble("speed")*3.6).roundToInt()) + " km/h"            // default is meter/sec

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
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = VISIBLE

                now = (SimpleDateFormat("HH:mm",Locale.ENGLISH)).format(Date(updateAt*1000))
                sunsetTime = (SimpleDateFormat("HH:mm",Locale.ENGLISH)).format(Date(sunset*1000))
                sunriseTime = (SimpleDateFormat("HH:mm",Locale.ENGLISH)).format(Date(sunrise*1000))

                if(itsNightNow()){
                    findViewById<RelativeLayout>(R.id.mainPage).setBackgroundResource(R.drawable.bg_gradient_night)
                    if (Build.VERSION.SDK_INT >= 21) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.black))
                    }
                } // end of if

            }
            catch(e: Exception)
            {
                if(itsNightNow()){
                    findViewById<RelativeLayout>(R.id.mainPage).setBackgroundResource(R.drawable.bg_gradient_night)
                }
                findViewById<ProgressBar>(R.id.loader).visibility = GONE
                findViewById<TextView>(R.id.errorText).text = ("Nem található a város az adatbázisban!\n" +
                        "Ellenőrizd az internetkapcsolatot!\n" +
                        "A város neve a következő formátumú:\n" +
                        "\"Városnév, ország rövidítve\": \"Debrecen, Hu\" ")
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.errorContainer).visibility = View.VISIBLE
            }
        }
    }

    fun plusImagePushed(view: View) {
        setContentView(R.layout.search_layout)
        if(itsNightNow()){
            findViewById<RelativeLayout>(R.id.searchLayout).setBackgroundResource(R.drawable.bg_gradient_night)
        }
    }

    fun okButtonPushed(view: View) {
        //CITY = findViewById<EditText>(R.id.editText).text.toString()
        //setContentView(R.layout.activity_main)
        //weatherTask().execute()
        var cityName = findViewById<EditText>(R.id.editText).text.toString()
        var listOfCityMatches = listOf<CityModel>().toMutableList()
        var i = 0
        while(i<cities.data.size){
            if (cities.data[i].name.equals(cityName))
            {
                listOfCityMatches.add(cities.data[i])
            }
            i++
        }
        var listView = findViewById<ListView>(R.id.listView)
        var list = mutableListOf<Model>()
        if(listOfCityMatches.size > 0){
            var i = 0
            while(i<listOfCityMatches.size)
            {
                list.add(Model(listOfCityMatches[i].name,listOfCityMatches[i].country))
                i++
            }
        }
        else{
            list.add(Model("Nincs találat",""))
        }
        listView.adapter = MyAdapter(this,R.layout.row,list)
        findViewById<TextView>(R.id.eredmeny).text = "Találatok: "+listOfCityMatches.size.toString()
        var selectedCity = CityModel()
        listView.setOnItemClickListener{parent:AdapterView<*>, view: View, position:Int, id:Long ->
            selectedCity = listOfCityMatches[position]
            updateCity(selectedCity.name+", "+selectedCity.country)
        }
    }
    fun updateCity(s:String){
        CITY = s
        setContentView(R.layout.activity_main)
        weatherTask().execute()
    }

    fun loadJson(context: Context): String {
        var input : InputStream? = null
        var jsonString: String

        try{
            input = context.assets.open("cities.json")

            val size = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            jsonString = String(buffer)
            return jsonString
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            input?.close()
        }
        return ""
    }

    fun backButtonPushed(view: View) {
        CITY = "Debrecen, HU"
        weatherTask().execute()
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.errorContainer).visibility = GONE
    }

    fun itsNightNow():Boolean{
        if(now > sunsetTime || now < sunriseTime)
        {
            return true
        }
        return false
    }
}

