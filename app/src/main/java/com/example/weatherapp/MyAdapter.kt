package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyAdapter (val mCtx: Context, var resources:Int, var items: List<Model>):ArrayAdapter<Model>(mCtx, resources, items){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View{
        val layoutInflater:LayoutInflater = LayoutInflater.from(mCtx)
        val view:View = layoutInflater.inflate(resources,null)
        val cityTextView: TextView = view.findViewById(R.id.textView1)
        val countryTextView: TextView = view.findViewById(R.id.textView2)
        var mItem:Model = items[position]
        cityTextView.text = mItem.city
        countryTextView.text = mItem.country

        return view
    }
}