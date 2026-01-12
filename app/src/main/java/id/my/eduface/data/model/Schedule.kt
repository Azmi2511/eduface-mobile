package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Schedule(
    val id: Int,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("teacher_id") val teacherId: Int,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val subject: Subject?,
    @SerializedName("class") val schoolClass: SchoolClass?,
    val teacher: Teacher?
)