package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val data: List<NotificationItem>,
    val meta: NotificationMeta?
)

data class NotificationMeta(
    @SerializedName("unread_count") val unreadCount: Int
)

data class NotificationItem(
    val id: Int,
    val message: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("ann_id") val annId: Int?,
    val link: String?,
    @SerializedName("time_ago") val timeAgo: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class UnreadCountResponse(
    @SerializedName("unread_count") val unreadCount: Int
)