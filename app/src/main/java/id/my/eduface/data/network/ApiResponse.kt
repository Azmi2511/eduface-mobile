package id.my.eduface.data.network

data class ListResponse<T>(
    val data: List<T>,
    val message: String?
)

data class SingleResponse<T>(
    val data: T,
    val message: String?
)