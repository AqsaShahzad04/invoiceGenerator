package com.learner.invoicegenerator.ui.adaptor

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlin.getValue

class InvoiceAdapter(
    private var clientsLists:List<Client>,
    private val selectClient:(Client)->Unit,
    private var selectedClientId: Int? = null

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
        val client = clientsLists[position]

        holder.profileAvatar.setText(AvatarUtils.getLetter(client.businessName))
        val color = AvatarUtils.getColor(client.businessName)
        holder.profileAvatar.background.setTint(Color.parseColor(color))
        holder.businessName.setText(client.businessName)
        holder.clientName.setText(client.contactPerson)

        val isSelected = client.id == selectedClientId
        holder.clientSelectedIcon.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.itemView.setBackgroundResource(
            if (isSelected) R.drawable.bg_new_invoice_selected_client else R.drawable.bg_new_invoice_client_profile
        )

        holder.itemView.setOnClickListener {
            selectClient(client)
        }
    }
    fun setSelectedClient(clientId: Int?) {
        selectedClientId = clientId
        notifyDataSetChanged()
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