package com.darklight_systems.financialapp.view

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.XmlParser
import kotlinx.android.synthetic.main.fragment_value_per_date.*
import java.util.*

class ValuePerDateFragment : Fragment() {

    private lateinit var selectDateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View?=inflater.inflate(R.layout.fragment_value_per_date, container, false)
        selectDateButton = view?.findViewById(R.id.select_date_button) as Button
        selectDateButton.setOnClickListener {
            openDobPicker(selected_date_tv)
        }
        return view
    }


    private fun openDobPicker(textView: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            val date = "${dayOfMonth}/${monthOfYear}/${year}"
            textView.text = date
            XmlParser().parseV2(requireActivity().applicationContext)
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time

        dpd.show()
    }
}