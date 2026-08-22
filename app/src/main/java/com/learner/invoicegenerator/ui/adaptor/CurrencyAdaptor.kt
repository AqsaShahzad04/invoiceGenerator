package com.learner.invoicegenerator.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.ui.model.Currency

class CurrencyAdaptor(var dataset: List<Currency>) : RecyclerView.Adapter<CurrencyAdaptor.currencyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): currencyViewHolder {
        var view= LayoutInflater.from(parent.context).inflate(R.layout.currency_row,parent,false)
         return currencyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: currencyViewHolder,
        position: Int
    ) {
        var isbtnChecked=dataset[position].isSelected
        holder.currencyCode.setText( dataset[position].code)
        holder.currencyName.setText(dataset[position].name)
        holder.currencyRadioBtn.isChecked=isbtnChecked
    }

    override fun getItemCount(): Int {
       return dataset.size
    }

    class currencyViewHolder(currencyRowview: View): RecyclerView.ViewHolder(currencyRowview){

        var currencyCode: TextView
        var currencyName: TextView
        var currencyRadioBtn: RadioButton

        init{
            currencyCode=currencyRowview.findViewById<TextView>(R.id.currencyCode)
            currencyName=currencyRowview.findViewById<TextView>(R.id.currencyName)
            currencyRadioBtn=currencyRowview.findViewById<RadioButton>(R.id.currencyRadioBtn)
        }



    }
}