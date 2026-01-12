package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Student(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    val nisn: String?,
    @SerializedName("class_id") val classId: Int?,
    @SerializedName("photo_path") val photoPath: String?,
    @SerializedName("face_registered") val faceRegistered: Boolean?,
    val user: User?,
    @SerializedName("class") val schoolClass: SchoolClass?
)