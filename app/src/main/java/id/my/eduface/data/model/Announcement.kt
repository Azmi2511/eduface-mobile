package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Announcement(
    val id: Int,
    val message: String?,
    @SerializedName("attachment_link") val attachmentLink: String?,
    @SerializedName("sent_at") val sentAt: String?,
    @SerializedName("created_at") val createdAt: String?
)