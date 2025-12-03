package hu.bme.aut.cryptocasino.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import hu.bme.aut.cryptocasino.data.api.BlockchainTransactionApi
import hu.bme.aut.cryptocasino.data.model.transaction.BlockchainTransaction
import retrofit2.HttpException
import java.io.IOException

class BlockchainTransactionPagingSource(
    private val api: BlockchainTransactionApi,
) : PagingSource<Int, BlockchainTransaction>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BlockchainTransaction> {
        val page = params.key ?: 0

        return try {
            val response = api.getMyTransactions(page, params.loadSize)

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }

            val data = response.body() ?: return LoadResult.Error(IOException("Empty response body"))

            val prevKey = if (page == 0) null else page - 1

            val isLastPage = data.last || data.content.isEmpty() || (data.totalPages > 0 && page >= data.totalPages - 1)
            val nextKey = if (isLastPage) null else page + 1

            LoadResult.Page(
                data = data.content,
                prevKey = prevKey,
                nextKey = nextKey,
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
        val key =
            state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)

                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        return key
    }
}
