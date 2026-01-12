package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class SchoolClass(
    val id: Int,
    @SerializedName("class_name") val className: String,
    @SerializedName("grade_level") val gradeLevel: Int,
    @SerializedName("academic_year") val academicYear: String
)