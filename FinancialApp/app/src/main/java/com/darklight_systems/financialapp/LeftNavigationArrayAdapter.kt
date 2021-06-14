package com.skynet.fish_shop.view.extension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.darklight_systems.financialapp.R

class LeftNavigationArrayAdapter(private val context: Context, private val data: Array<String>) :
    BaseAdapter() {
    private val inflater: LayoutInflater = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.drawer_list_item, viewGroup, false)
        }
        val item = data[i]
        (view?.findViewById<View>(R.id.left_menu_item_text) as TextView).text = item
        return view
    }

}