package com.learner.invoicegenerator.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine

class InvoiceItemsReviewAdapter(var itemsList:List<InvoiceItemLine>): RecyclerView.Adapter<InvoiceItemsReviewAdapter.ReviewItemsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewItemsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_in_invoice_review_row,parent,false)
        return ReviewItemsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ReviewItemsViewHolder,
        position: Int
    ) {
        val data=itemsList[position]
        holder.itemMultiplyQuantity.text=data.itemName+" * "+data.itemQuantity.toInt().toString()
        holder.itemMultiplyPrice.text = String.format("%.2f", data.unitPrice * data.itemQuantity)
    }

    override fun getItemCount(): Int {
      return itemsList.size
    }

    class ReviewItemsViewHolder( itemView: View): RecyclerView.ViewHolder(itemView){

        lateinit var itemMultiplyQuantity: TextView
        lateinit var itemMultiplyPrice: TextView

        init{
            itemMultiplyQuantity=itemView.findViewById<TextView>(R.id.itemAndQuantity)
            itemMultiplyPrice=itemView.findViewById<TextView>(R.id.itemPrice)
        }
    }
}