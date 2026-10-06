package com.learner.invoicegenerator.ui.clients.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.ClientRowBinding
import com.learner.invoicegenerator.utils.AvatarUtils

data class ClientRowStats(
    val totalBilled: Double = 0.0,
    val invoiceCount: Int = 0
)

class ClientAdapter(
    private var clients: List<Client>,
    private var statsMap: Map<Int, ClientRowStats> = emptyMap(),
    private var currencySymbol: String = "$",
    private val onClientClick: (Client) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    class ClientViewHolder(val binding: ClientRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ClientRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]
        val stats = statsMap[client.id] ?: ClientRowStats()

        holder.binding.BusinessName.text = client.businessName
        holder.binding.clientname.text = client.contactPerson ?: ""

        val letter = AvatarUtils.getLetter(client.businessName)
        val color = AvatarUtils.getColor(client.businessName)

        holder.binding.clientProfile.text = letter
        holder.binding.clientProfile.background.setTint(Color.parseColor(color))

        val formattedAmount = if (stats.totalBilled % 1.0 == 0.0) {
            stats.totalBilled.toLong().toString()
        } else {
            String.format("%.2f", stats.totalBilled)
        }
        holder.binding.amount.text = if (currencySymbol.isEmpty()) formattedAmount else "$currencySymbol $formattedAmount"

        val countText = if (stats.invoiceCount == 1) "1 invoice" else "${stats.invoiceCount} invoices"
        holder.binding.invoicesCount.text = countText

        holder.itemView.setOnClickListener { onClientClick(client) }
    }

    override fun getItemCount(): Int = clients.size

    fun updateData(
        newList: List<Client>,
        newStatsMap: Map<Int, ClientRowStats> = statsMap,
        symbol: String = currencySymbol
    ) {
        clients = newList
        statsMap = newStatsMap
        currencySymbol = symbol
        notifyDataSetChanged()
    }
}