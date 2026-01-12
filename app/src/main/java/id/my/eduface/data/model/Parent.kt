package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Parent(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    val relationship: String?,
    val user: User?
)