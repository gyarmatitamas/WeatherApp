package com.example.weatherapp

import com.google.gson.annotations.SerializedName

class ListCityModel {
    @SerializedName("data")
    var data: ArrayList<CityModel> = ArrayList()
}