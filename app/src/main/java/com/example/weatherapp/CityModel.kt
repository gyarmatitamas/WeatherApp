package com.example.weatherapp

import com.google.gson.annotations.SerializedName

class CityModel {
    @SerializedName("id")
    var id:Int = 0

    @SerializedName("name")
    var name:String = ""

    @SerializedName("state")
    var state:String = ""

    @SerializedName("country")
    var country:String = ""

}
