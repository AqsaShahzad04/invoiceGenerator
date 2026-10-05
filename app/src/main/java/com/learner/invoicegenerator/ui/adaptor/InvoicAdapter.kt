package com.learner.invoicegenerator.ui.adaptor

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoiceAdapter(
    var invoicesList: List<Invoice>,
    private val onInvoiceClick: ((Invoice) -> Unit)? = null
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.invoice_row, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val data = invoicesList[position]
        val context = holder.itemView.context

        holder.clientName.text = data.clientBusinessName
        holder.invoiceNumber.text = if (data.invoiceNum.length >= 4) data.invoiceNum.takeLast(4) else data.invoiceNum
        holder.invoiceDate.text = data.issueDate.format(formatter)
        holder.amount.text = String.format("%.2f", data.totalAmount)

        val paymentStatus = data.status
        when {
            paymentStatus == "Paid" -> {
                holder.statusText.text = "Paid"
                holder.statusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
                holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.primary_green))
            }
            data.dueDate.isBefore(LocalDate.now()) -> {
                holder.statusText.text = "Unpaid"
                holder.statusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.carrot_red_shade))
                holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.carrot_red_shade))
            }
            else -> {
                holder.statusText.text = "Pending"
                holder.statusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange_red_shade))
                holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.orange_red_shade))
            }
        }

        holder.itemView.setOnClickListener {
            onInvoiceClick?.invoke(data)
        }
    }

    override fun getItemCount(): Int = invoicesList.size

    class InvoiceViewHolder(invoiceView: View) : RecyclerView.ViewHolder(invoiceView) {
        val clientName: TextView = invoiceView.findViewById(R.id.client_name)
        val invoiceNumber: TextView = invoiceView.findViewById(R.id.invoice_number)
        val invoiceDate: TextView = invoiceView.findViewById(R.id.invoice_date)
        val amount: TextView = invoiceView.findViewById(R.id.amount)
        val statusDot: View = invoiceView.findViewById(R.id.status_dot)
        val statusText: TextView = invoiceView.findViewById(R.id.status_text)
    }
}
