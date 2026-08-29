package com.learner.invoicegenerator.ui.adaptor

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.utils.AvatarUtils

class InvoiceAdapter(
    private var clientsLists:List<Client>,
    private val selectClient:(Client)->Unit

): RecyclerView.Adapter<InvoiceAdapter.invoiceViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): invoiceViewHolder {
       val view= LayoutInflater.from(parent.context).inflate(R.layout.new_invoice_select_client_row,parent,false)
        return invoiceViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: invoiceViewHolder,
        position: Int
    ) {
        holder.profileAvatar.setText(AvatarUtils.getLetter(clientsLists[position].businessName))
        val color = AvatarUtils.getColor(clientsLists[position].businessName)
        holder.profileAvatar.background.setTint(Color.parseColor(color))
        holder.businessName.setText(clientsLists[position].businessName)
        holder.clientName.setText(clientsLists[position].contactPerson)
        holder.clientSelectedIcon.visibility=View.GONE

        holder.itemView.setOnClickListener {
            holder.clientSelectedIcon.visibility=View.VISIBLE
            holder.itemView.setBackgroundResource(R.drawable.bg_new_invoice_selected_client)
            selectClient(clientsLists[position])
        }

    }

    override fun getItemCount(): Int {
       return clientsLists.size
    }

    class invoiceViewHolder(clientRowView: View): RecyclerView.ViewHolder(clientRowView){
      lateinit var profileAvatar: TextView
      lateinit var businessName: TextView
      lateinit var clientName: TextView
      lateinit var clientSelectedIcon: ImageView

      init {
          profileAvatar=clientRowView.findViewById<TextView>(R.id.profileCircle)
          businessName=clientRowView.findViewById<TextView>(R.id.businessName)
          clientName=clientRowView.findViewById<TextView>(R.id.name)
          clientSelectedIcon=clientRowView.findViewById<ImageView>(R.id.selectedClientIcon)
      }
    }
}