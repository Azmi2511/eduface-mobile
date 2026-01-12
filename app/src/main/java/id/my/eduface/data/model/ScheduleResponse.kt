package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleBaseResponse(
    @SerializedName("data") val data: List<ScheduleResponse>
)

data class ScheduleResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("time_range") val timeRange: String,
    @SerializedName("subject") val subject: SubjectInfo,
    @SerializedName("class") val classInfo: ClassInfo,
    @SerializedName("teacher") val teacher: TeacherInfo,
    @SerializedName("color_hex") val colorHex: String? = "#D1FAE5"
)

data class SubjectInfo(
    @SerializedName("name") val name: String
)

data class ClassInfo(
    @SerializedName("name") val name: String
)

data class TeacherInfo(
    @SerializedName("full_name") val fullName: String
)