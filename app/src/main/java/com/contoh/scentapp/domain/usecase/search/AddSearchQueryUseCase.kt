package com.contoh.scentapp.domain.usecase.search

import com.contoh.scentapp.data.repository.SearchHistoryRepository

class AddSearchQueryUseCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke(query: String) {
        repository.addSearchQuery(query)
    }
}
