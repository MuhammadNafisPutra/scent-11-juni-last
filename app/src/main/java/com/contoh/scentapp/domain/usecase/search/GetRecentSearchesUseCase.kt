package com.contoh.scentapp.domain.usecase.search

import com.contoh.scentapp.data.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetRecentSearchesUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getRecentSearches()
    }
}
