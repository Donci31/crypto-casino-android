package hu.bme.aut.crypto_casino_android.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import retrofit2.HttpException
import java.io.IOException

class BlockchainTransactionPagingSource(
    private val api: BlockchainTransactionApi
) : PagingSource<Int, BlockchainTransaction>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BlockchainTransaction> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val response = api.getMyTransactions(page, pageSize)

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }

            val data = response.body()
            if (data == null) {
                return LoadResult.Error(IOException("Empty response body"))
            }

            val transactions = data.content
            val nextKey = if (data.last) null else page + 1
            val prevKey = if (page > 0) page - 1 else null

            LoadResult.Page(
                data = transactions,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, BlockchainTransaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
