package com.learner.invoicegenerator.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine

class ItemsFinalInvoiceAdapter(var itemsList:List<InvoiceItemLine>,var currencySymbol:String): RecyclerView.Adapter<ItemsFinalInvoiceAdapter.ItemsFinalInvoiceViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemsFinalInvoiceViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.final_invoice_items_row,parent,false)
        return ItemsFinalInvoiceViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemsFinalInvoiceViewHolder,
        position: Int
    ) {
        val data=itemsList[position]
        holder.itemName.text=data.itemName
        holder.itemRate.text="$currencySymbol ${data.unitPrice}/${data.itemUnit}"
        holder.itemQuantity.text=data.itemQuantity.toString()
        holder.itemAmount.text=(data.unitPrice*data.itemQuantity).toString()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class ItemsFinalInvoiceViewHolder(itemsView: View): RecyclerView.ViewHolder(itemsView){
        lateinit var itemName: TextView
        lateinit var itemRate: TextView
        lateinit var itemQuantity: TextView
        lateinit var itemAmount: TextView

        init{
            itemName=itemsView.findViewById<TextView>(R.id.tvItemName)
            itemRate=itemsView.findViewById<TextView>(R.id.tvItemRate)
            itemQuantity=itemsView.findViewById<TextView>(R.id.tvItemQty)
            itemAmount=itemsView.findViewById<TextView>(R.id.tvItemAmount)
        }
    }
}