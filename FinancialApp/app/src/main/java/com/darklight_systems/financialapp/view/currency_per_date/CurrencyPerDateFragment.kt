package com.darklight_systems.financialapp.view.currency_per_date

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.CurrencyParser
import com.darklight_systems.financialapp.controller.convertToDateFromLocalDate
import com.darklight_systems.financialapp.controller.downloadUrl
import com.darklight_systems.financialapp.controller.parseFromLocalDateToString
import com.darklight_systems.financialapp.model.Currency
import com.darklight_systems.financialapp.model.GET_ALL_CURRENCY_URL
import kotlinx.android.synthetic.main.fragment_currency_per_date.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class CurrencyPerDateFragment : Fragment() {

    private lateinit var selectDateButton: Button
    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var selectedDate: LocalDate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_currency_per_date, container, false)
        setSelectDateButton(view)
        setAdapter(view)
        setCurrentDate(view)
        return view
    }

    private fun setCurrentDate(view: View?) {
        selectedDate = LocalDate.now()
        val date = parseFromLocalDateToString(selectedDate,"dd/MM/yyyy")
        (view?.findViewById(R.id.selected_date_tv) as TextView).text = date
        DownloadCurrenciesTask().execute(GET_ALL_CURRENCY_URL(date))
    }

    private fun setSelectDateButton(view: View?) {
        selectDateButton = view?.findViewById(R.id.select_date_button) as Button
        selectDateButton.setOnClickListener {
            openDatePicker(selected_date_tv)
        }
    }

    private fun setAdapter(view: View?) {
        val recyclerView =
            (view?.findViewById(R.id.currency_rv) as RecyclerView)
        currencyAdapter = CurrencyAdapter(ArrayList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = currencyAdapter
    }


    private fun openDatePicker(textView: TextView) {
        val year = selectedDate.year
        val month = selectedDate.monthValue
        val day = selectedDate.dayOfMonth

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            selectedDate = LocalDate.of(year, monthOfYear, dayOfMonth)
            val parsedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val parsedMonthOfYear =
                if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
            val date = "${parsedDayOfMonth}/${parsedMonthOfYear}/${year}"
            textView.text = date
            DownloadCurrenciesTask().execute(GET_ALL_CURRENCY_URL(date))
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time

        dpd.show()
    }

    private inner class DownloadCurrenciesTask : AsyncTask<String, Void, List<Currency>>() {

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
                currencyAdapter.updateData(result as ArrayList<Currency>)
            }
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): List<Currency> {
            downloadUrl(urlString)?.use { stream ->
                context?.let {
                    return CurrencyParser().parse(it, stream, convertToDateFromLocalDate(selectedDate))
                }
            } ?: return emptyList()
        }
    }
}