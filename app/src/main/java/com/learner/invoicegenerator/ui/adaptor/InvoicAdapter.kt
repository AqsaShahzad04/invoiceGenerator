package com.learner.invoicegenerator.ui.adaptor

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Invoice

class InvoicAdapter(var invoicesList:List<Invoice>): RecyclerView.Adapter<InvoicAdapter.InvoiceViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvoiceViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: InvoiceViewHolder,
        position: Int
    ) {


        }


    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }


    class InvoiceViewHolder(invoiceView: View): RecyclerView.ViewHolder(invoiceView){
        class InvoiceViewHolder(invoiceView: View) : RecyclerView.ViewHolder(invoiceView) {

            private val clientAvatar: TextView = invoiceView.findViewById(R.id.client_avatar)
            private val clientName: TextView = invoiceView.findViewById(R.id.client_name)
            private val invoiceNumber: TextView = invoiceView.findViewById(R.id.invoice_number)
            private val invoiceDate: TextView = invoiceView.findViewById(R.id.invoice_date)
            private val amount: TextView = invoiceView.findViewById(R.id.amount)
            private val statusDot: View = invoiceView.findViewById(R.id.status_dot)
            private val statusText: TextView = invoiceView.findViewById(R.id.status_text)


        }
    }

}