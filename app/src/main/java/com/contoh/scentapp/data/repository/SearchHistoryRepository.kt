package com.contoh.scentapp.data.repository

import com.contoh.scentapp.data.local.dao.SearchHistoryDao
import com.contoh.scentapp.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchHistoryRepository(private val searchHistoryDao: SearchHistoryDao) {

    fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { entities ->
            entities.map { it.query }
        }
    }

    suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearchQuery(SearchHistoryEntity(query = query))
        }
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearHistory()
    }
}
