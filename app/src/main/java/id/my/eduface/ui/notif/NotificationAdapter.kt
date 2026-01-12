package id.my.eduface.ui.notif

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.my.eduface.databinding.ItemNotificationRowBinding
import id.my.eduface.data.model.NotificationItem

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNotificationRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvNotificationMessage.text = item.message
            binding.tvNotificationTime.text = item.timeAgo
            binding.viewUnreadIndicator.visibility = if (item.isRead) View.GONE else View.VISIBLE

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun setData(newList: List<NotificationItem>) {
        this.notifications = newList
        notifyDataSetChanged()
    }
}