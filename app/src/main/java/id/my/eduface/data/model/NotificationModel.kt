package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class NotificationModel(
    val id: Int,
    val message: String,
    @SerializedName("is_read") val isRead: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("ann_id") val announcementId: Int?
)