package com.learner.invoicegenerator.ui.clients.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.ClientRowBinding
import com.learner.invoicegenerator.utils.AvatarUtils

class ClientAdapter(
    private var clients: List<Client>,
    private val onClientClick: (Client) -> Unit
) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    private val avatarColors = listOf(
        "#876B5F",
        "#A87C5F",
        "#7C8E70",
        "#8E6B70",
        "#5F6F87"
    )

    // ViewHolder — ek row ke views ka reference rakhta hai
    class ClientViewHolder(val binding: ClientRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Naya khaali row banao (template)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ClientRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClientViewHolder(binding)
    }

    // Row mein actual data bharo
    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]

        holder.binding.BusinessName.text = client.businessName
        holder.binding.clientname.text = client.contactPerson ?: ""

        val letter = AvatarUtils.getLetter(client.businessName)
        val color = AvatarUtils.getColor(client.businessName)

        holder.binding.clientProfile.text = letter
        holder.binding.clientProfile.background.setTint(Color.parseColor(color))

        holder.binding.amount.text = "0"
        holder.binding.invoicesCount.text = "0 invoices"
        holder.itemView.setOnClickListener { onClientClick(client) }
    }
    // Total items kitne hain
    override fun getItemCount(): Int = clients.size


    fun updateList(newList: List<Client>) {
        clients = newList
        notifyDataSetChanged()
    }
}