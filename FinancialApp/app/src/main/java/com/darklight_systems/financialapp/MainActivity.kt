package com.darklight_systems.financialapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tool_bar_with_menu_button.*

class MainActivity : AppCompatActivity() {

    lateinit var leftMenuTitlesArray:Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createToolbar()
    }

    private fun createToolbar() {
        setSupportActionBar(tool_bar)
    }
}