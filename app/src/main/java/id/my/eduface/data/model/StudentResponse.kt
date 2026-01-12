package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class StudentResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<StudentItem>
)

data class StudentItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("nisn")
    val nisn: String? = null,
    @SerializedName("class_name")
    val className: String? = null
)