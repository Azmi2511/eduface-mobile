package id.my.eduface.ui.permission

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.my.eduface.R
import id.my.eduface.data.model.PermissionItem

class PermissionAdapter(private val list: List<PermissionItem>) :
    RecyclerView.Adapter<PermissionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Sesuaikan ID ini dengan item_permission.xml Anda
        val tvName: TextView = view.findViewById(R.id.tv_student_name)
        val tvType: TextView = view.findViewById(R.id.tv_permission_type)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvDate: TextView = view.findViewById(R.id.tv_date_range)
        val indicator: View = view.findViewById(R.id.status_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val studentName = item.student?.fullName ?: "Siswa"
        holder.tvName.text = studentName

        // Mapping description -> reason (sesuai JSON API)
        val reasonText = item.description ?: "-"
        holder.tvType.text = "${item.type} - $reasonText"

        val start = item.dateRange?.start ?: "?"
        val end = item.dateRange?.end ?: "?"
        holder.tvDate.text = "$start s/d $end"

        val statusText = item.approvalStatus ?: "Pending"
        holder.tvStatus.text = statusText

        val colorCode = when (statusText.lowercase()) {
            "approved" -> "#4CAF50"
            "rejected" -> "#F44336"
            else -> "#FF9800"
        }

        try {
            val color = Color.parseColor(colorCode)
            holder.tvStatus.setTextColor(color)
            holder.indicator.setBackgroundColor(color)
        } catch (e: Exception) {
            holder.tvStatus.setTextColor(Color.DKGRAY)
            holder.indicator.setBackgroundColor(Color.DKGRAY)
        }
    }

    override fun getItemCount() = list.size
}