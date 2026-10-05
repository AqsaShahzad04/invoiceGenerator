package com.learner.invoicegenerator.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.AppNotification

class NotificationAdapter(
    var notifications: List<AppNotification>,
    private val onItemClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_row, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        holder.title.text = item.title
        holder.message.text = item.message
        holder.time.text = getRelativeTimeString(item.timestamp)

        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val time: TextView = itemView.findViewById(R.id.notificationTime)
        val unreadDot: View = itemView.findViewById(R.id.unreadDot)
    }

    private fun getRelativeTimeString(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            else -> "${days}d ago"
        }
    }
}
