package com.pvbcsoft.currencyconverter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CurrencySpinnerAdapter(context: Context, currencies: List<Currency>) :
    ArrayAdapter<Currency>(context, android.R.layout.simple_spinner_dropdown_item, currencies) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val currency = getItem(position)
        (view as TextView).text = currency?.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val currency = getItem(position)
        (view as TextView).text = currency?.name
        return view
    }
}
