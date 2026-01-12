package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceWrapper(
    @SerializedName("data")
    val data: List<AttendanceItem>
)

data class AttendanceItem(
    @SerializedName("attendance") val attendance: AttendanceData,
    @SerializedName("student") val student: StudentData,
    @SerializedName("schedule") val schedule: ScheduleData
)

data class AttendanceData(
    val id: Int,
    val status: String?,
    val date: String?,
    val time: String?
)

data class StudentData(
    val nisn: String?,
    val name: String?
)

data class ScheduleData(
    val subject: String?,
    val teacher: String?,
    @SerializedName("class") val className: String?
)