package hu.bme.aut.crypto_casino_android.data.model.transaction

data class PagedResponse<T>(
    val content: List<T>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
) {
    data class Pageable(
        val offset: Long,
        val pageNumber: Int,
        val pageSize: Int,
        val paged: Boolean,
        val sort: Sort
    )

    data class Sort(
        val empty: Boolean,
        val sorted: Boolean,
        val unsorted: Boolean
    )
}
