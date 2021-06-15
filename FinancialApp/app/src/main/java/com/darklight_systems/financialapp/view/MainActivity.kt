package com.darklight_systems.financialapp.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.view.currency_per_date.ValuePerDateFragment
import com.darklight_systems.financialapp.view.values_history.ValuesHistoryFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tool_bar_with_menu_button.*

class MainActivity : AppCompatActivity() {

    private lateinit var leftMenuTitlesArray: Array<String>
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var valuePerDateFragment: ValuePerDateFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        leftMenuTitlesArray = resources.getStringArray(R.array.left_menu_titles)
        valuePerDateFragment = ValuePerDateFragment()
        createToolbar()
        createLeftNavigationMenu()
        createValuePerDateFragment()
    }

    private fun createToolbar() {
        tool_bar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_bikini_60_s_menu)
        setSupportActionBar(tool_bar)
    }

    private fun createLeftNavigationMenu() {
        left_drawer.adapter = LeftNavigationArrayAdapter(this, leftMenuTitlesArray)
        left_drawer.onItemClickListener = DrawerItemClickListener()
        mDrawerToggle = ActionBarDrawerToggle(
            this,
            main_drawer_layout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        main_drawer_layout.setDrawerListener(mDrawerToggle)
    }

    private fun createValuePerDateFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_relative_layout, valuePerDateFragment)
        ft.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (mDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item!!)
    }

    private inner class DrawerItemClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            checkoutToSelectedFragment(position)
        }
    }

    private fun checkoutToSelectedFragment(position: Int) {
        val fragment: Fragment =
            when (position) {
                0 -> ValuePerDateFragment()
                1 -> ValuesHistoryFragment()
                2 -> ConverterFragment()
                else -> ValuePerDateFragment()
            }
        changeFragment(fragment, position)
        left_drawer.setItemChecked(position, true)
        main_drawer_layout.closeDrawer(left_menu_linear_layout)
    }

    private fun changeFragment(fragment: Fragment?, position: Int) {
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.main_relative_layout, fragment).commit()
            left_drawer.setItemChecked(position, true)
            main_drawer_layout.closeDrawer(left_menu_linear_layout)
        } else {
            Log.e(this.javaClass.name, "Error. Fragment is not created")
        }
    }
}