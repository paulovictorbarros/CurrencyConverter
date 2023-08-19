package com.pvbcsoft.currencyconverter.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.gson.JsonObject
import com.pvbcsoft.currencyconverter.api.Endpoint
import com.pvbcsoft.currencyconverter.network.NetworkUtils
import retrofit2.Call
import retrofit2.Response
import com.pvbcsoft.currencyconverter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btConvert.setOnClickListener { convertMoney() }

        getCurrencies()
    }

    private fun convertMoney() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val fromCurrency = binding.spFrom.selectedItem.toString().substringBefore(" - ")
        val toCurrency = binding.spTo.selectedItem.toString().substringBefore(" - ")

        val inputValue = binding.etValueFrom.text.toString()

        if (inputValue.isBlank()) {
            Toast.makeText(this@MainActivity, "Digite um valor para converter", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val amount = inputValue.toDoubleOrNull()

        if (amount == null || inputValue.endsWith(".") || amount == 0.0) {
            Toast.makeText(this, "Valor inválido inserido.", Toast.LENGTH_SHORT).show()
            return
        }

        endpoint.getCurrencyRate(fromCurrency, toCurrency)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val data = response.body()?.entrySet()?.find { it.key == toCurrency }
                        val rate: Double = data?.value?.asDouble ?: 0.0
                        val conversion = amount * rate

                        val formattedConversion = String.format("%.2f", conversion)
                        binding.tvResult.text = formattedConversion
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Falha ao obter taxa de câmbio",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Falha na requisição de rede",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getCurrencies() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        endpoint.getCurrencies().enqueue(object : retrofit2.Callback<JsonObject> {
            @SuppressLint("DiscouragedApi")
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val data = response.body()

                    val currenciesList = mutableListOf<String>()

                    data?.keySet()?.forEach { currencyCode ->
                        val currencyNameResId =
                            resources.getIdentifier("currency_$currencyCode", "string", packageName)
                        val currencyName = if (currencyNameResId != 0) {
                            getString(currencyNameResId)
                        } else {
                            return@forEach
                        }

                        val currencyInfo = "$currencyCode - $currencyName"
                        currenciesList.add(currencyInfo)
                    }

                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        currenciesList
                    )
                    binding.spFrom.adapter = adapter
                    binding.spTo.adapter = adapter

                    val defaultFrom =
                        currenciesList.indexOfFirst { it.startsWith("brl", ignoreCase = true) }
                    val defaultTo =
                        currenciesList.indexOfFirst { it.startsWith("usd", ignoreCase = true) }
                    binding.spFrom.setSelection(defaultFrom)
                    binding.spTo.setSelection(defaultTo)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Falha ao obter dados das moedas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Falha na requisição de rede", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}