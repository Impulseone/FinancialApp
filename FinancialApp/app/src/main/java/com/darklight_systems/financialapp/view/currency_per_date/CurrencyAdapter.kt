package com.darklight_systems.financialapp.view.currency_per_date

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.model.Currency

class CurrencyAdapter(private var currencyList: ArrayList<Currency>) :
    RecyclerView.Adapter<CurrencyAdapter.CurrencyTileView>() {

    fun updateData(currencyListNew: ArrayList<Currency>){
        currencyList = currencyListNew
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyTileView {
        return CurrencyTileView(LayoutInflater.from(parent.context).inflate(R.layout.currency_tile_view,parent,false))
    }

    override fun onBindViewHolder(holder: CurrencyTileView, position: Int) {
        holder.currencyName?.text = currencyList[position].name
        holder.currencyValue?.text = currencyList[position].value.toString()
    }

    override fun getItemCount() = currencyList.size

    class CurrencyTileView(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var currencyName:TextView? = null
        var currencyValue:TextView? = null

        init {
            currencyName = itemView.findViewById(R.id.currency_name)
            currencyValue = itemView.findViewById(R.id.currency_value)
        }
    }
}