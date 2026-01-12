package id.my.eduface.ui.attendance

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.my.eduface.R
import id.my.eduface.data.model.AttendanceItem
import id.my.eduface.databinding.ItemAttendanceBinding

class AttendanceAdapter(
    private var attendanceList: List<AttendanceItem>,
    private val onItemClick: (AttendanceItem) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    fun setData(newList: List<AttendanceItem>) {
        this.attendanceList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendanceList[position])
        holder.itemView.setOnClickListener {
            onItemClick(attendanceList[position])
        }
    }

    override fun getItemCount(): Int = attendanceList.size

    inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AttendanceItem) {
            val attendance = item.attendance
            val schedule = item.schedule

            binding.tvSubject.text = schedule.subject ?: "Masuk Sekolah"
            binding.tvStatus.text = attendance.status ?: "-"
            binding.tvTime.text = "${attendance.date} • ${attendance.time} WIB"

            val context = binding.root.context
            val currentStatus = (attendance.status ?: "").lowercase()

            val color = when (currentStatus) {
                "hadir" -> ContextCompat.getColor(context, R.color.success_green)
                "terlambat" -> ContextCompat.getColor(context, R.color.warning_orange)
                "izin", "sakit" -> ContextCompat.getColor(context, R.color.info_blue)
                else -> ContextCompat.getColor(context, R.color.error_red)
            }

            binding.tvStatus.setTextColor(color)
            binding.viewStatusTag.setBackgroundColor(color)
        }
    }
}