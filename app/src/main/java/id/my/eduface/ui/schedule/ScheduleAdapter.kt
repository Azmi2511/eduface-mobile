package id.my.eduface.ui.schedule

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.my.eduface.R
import id.my.eduface.data.model.ScheduleResponse

class ScheduleAdapter(
    private var list: List<ScheduleResponse>,
    private val onItemClick: (ScheduleResponse) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    fun setData(newList: List<ScheduleResponse>) {
        this.list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tv_subject_name)
        val tvDayTime: TextView = view.findViewById(R.id.tv_day_time)
        val viewTag: View = view.findViewById(R.id.view_color_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSubject.text = item.subject.name
        holder.tvDayTime.text = "${item.dayOfWeek} | ${item.timeRange}"

        try {
            val color = Color.parseColor(item.colorHex ?: "#D1FAE5")
            holder.viewTag.backgroundTintList = ColorStateList.valueOf(color)
        } catch (e: Exception) {
            holder.viewTag.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size
}