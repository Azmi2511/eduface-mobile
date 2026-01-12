package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Teacher(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    val nip: String?,
    @SerializedName("employment_status") val employmentStatus: String?,
    @SerializedName("teacher_code") val teacherCode: String?,
    val user: User?
)