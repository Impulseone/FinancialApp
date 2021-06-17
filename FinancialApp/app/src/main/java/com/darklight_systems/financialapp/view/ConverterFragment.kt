package com.darklight_systems.financialapp.view

import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.AllCurrenciesParser
import com.darklight_systems.financialapp.controller.convertToDateFromLocalDate
import com.darklight_systems.financialapp.controller.downloadUrl
import com.darklight_systems.financialapp.controller.parseFromLocalDateToString
import com.darklight_systems.financialapp.model.CURRENCY_HISTORY_URL
import com.darklight_systems.financialapp.model.Currency
import com.darklight_systems.financialapp.model.GET_ALL_CURRENCY_URL
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.time.LocalDate

class ConverterFragment : Fragment() {

    private var selectedFromCurrency: String = ""
    private var selectedToCurrency: String = ""
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private var allCurrencies:ArrayList<Currency> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_converter, container, false)
        initSpinners(view)
        DownloadCurrencyTask().execute(GET_ALL_CURRENCY_URL(parseFromLocalDateToString(LocalDate.now(),"dd.MM.yyyy")))
        return view
    }

    private fun initSpinners(view: View) {
        spinnerFrom = view.findViewById<Spinner>(R.id.from_currency_spinner)
        spinnerFrom.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedFromCurrency = (spinnerFrom.selectedItem as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerTo = view.findViewById<Spinner>(R.id.to_currency_spinner)
        spinnerTo.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedToCurrency = spinnerTo.selectedItem as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private inner class DownloadCurrencyTask : AsyncTask<String, Void, List<Currency>>() {
        override fun doInBackground(vararg urls: String): List<Currency> {
            return try {
                loadXmlFromNetwork(urls[0])
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                emptyList()
            }
        }

        override fun onPostExecute(result: List<Currency>?) {
            if (result != null) {
                allCurrencies = result as ArrayList<Currency>
                val currencyNames: ArrayList<String> = ArrayList()
                for (element in result) {
                    currencyNames.add(element.name)
                }
                val adapter: ArrayAdapter<String>? = context?.let {
                    ArrayAdapter(
                        it,
                        android.R.layout.simple_spinner_item,
                        currencyNames
                    )
                }
                spinnerFrom.adapter = adapter
                spinnerTo.adapter = adapter
            }
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): List<Currency> {
            downloadUrl(urlString)?.use { stream ->
                context?.let {
                    return AllCurrenciesParser().parse(
                        it,
                        stream,
                        convertToDateFromLocalDate(LocalDate.now())
                    )
                }
            } ?: return emptyList()
        }
    }
}