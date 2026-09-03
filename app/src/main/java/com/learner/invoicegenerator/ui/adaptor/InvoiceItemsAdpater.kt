package com.learner.invoicegenerator.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine

class InvoiceItemsAdpater(var itemsList:List<InvoiceItemLine>,
                          val currencySymbol:String,
                          val onIncrement:(Int)->Unit,
                           val ondecrement:(Int)->Unit): RecyclerView.Adapter<InvoiceItemsAdpater.itemsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): itemsViewHolder {
        val row= LayoutInflater.from(parent.context).inflate(R.layout.item_in_invoice_row,parent,false)
        return itemsViewHolder(row)
    }

    override fun onBindViewHolder(
        holder: itemsViewHolder,
        position: Int
    ) {

            var itemsData= itemsList[position]
            holder.itemName.setText(itemsData.itemName)
            holder.itemPrice.text=(itemsData.unitPrice.toInt()*itemsData.itemQuantity).toString()
            holder.itemQuantity.text=itemsData.itemQuantity.toInt().toString()
            holder.itemDetail.setText(currencySymbol+itemsData.unitPrice.toInt().toString()+"."+itemsData.itemUnit)
            holder.incBtn.setOnClickListener { onIncrement(itemsData.id) }
            holder.decBtn.setOnClickListener { ondecrement(itemsData.id) }
    }

    override fun getItemCount(): Int {
      return itemsList.size
    }

    class itemsViewHolder(itemRowView: View): RecyclerView.ViewHolder(itemRowView){
       lateinit var itemName: TextView
       lateinit var itemDetail: TextView
       lateinit var itemQuantity: TextView
       lateinit var itemPrice: TextView

       lateinit var incBtn: TextView

       lateinit var decBtn: TextView

       init{
           itemName=itemRowView.findViewById<TextView>(R.id.itemName)
           itemDetail=itemRowView.findViewById<TextView>(R.id.itemDetails)
           itemQuantity=itemRowView.findViewById<TextView>(R.id.itemQuantity)
           itemPrice=itemRowView.findViewById<TextView>(R.id.itemPrice)
           incBtn=itemRowView.findViewById<TextView>(R.id.incBtn)
           decBtn=itemRowView.findViewById<TextView>(R.id.decBtn)
       }
    }
}