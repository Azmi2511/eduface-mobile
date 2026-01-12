package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceLog(
    val id: Int,
    @SerializedName("student_name") val studentName: String?,
    val status: String?,
    val time: String?,
    val date: String?
)