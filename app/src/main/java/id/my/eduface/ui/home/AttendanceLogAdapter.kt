package id.my.eduface.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.my.eduface.R
import id.my.eduface.data.model.AttendanceLog
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceLogAdapter(private val list: List<AttendanceLog>) :
    RecyclerView.Adapter<AttendanceLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_notification_message)
        val desc: TextView = view.findViewById(R.id.tv_notification_message)
        val time: TextView = view.findViewById(R.id.tv_notification_time)
        val indicator: View = view.findViewById(R.id.view_unread_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val rawStatus = item.status ?: "Hadir"
        val studentName = item.studentName ?: "Siswa"

        holder.title.text = "${rawStatus.replaceFirstChar { it.uppercase() }} - $studentName"
        holder.desc.text = "Masuk Sekolah • ${formatDate(item.date ?: "")}"
        holder.time.text = item.time ?: "--:--"

        val colorCode = when (rawStatus.lowercase()) {
            "present", "hadir" -> "#4CAF50"
            "late", "terlambat" -> "#FF9800"
            "permit", "izin", "sakit" -> "#2196F3"
            else -> "#F44336"
        }
        holder.indicator.background?.setTint(Color.parseColor(colorCode))
    }

    override fun getItemCount() = list.size

    private fun formatDate(dateString: String): String {
        if (dateString.isEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}