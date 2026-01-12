package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Subject(
    val id: Int,
    @SerializedName("subject_name") val subjectName: String
)