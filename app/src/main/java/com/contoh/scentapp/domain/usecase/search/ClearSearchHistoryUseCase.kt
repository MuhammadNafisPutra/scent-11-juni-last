package com.contoh.scentapp.domain.usecase.search

import com.contoh.scentapp.data.repository.SearchHistoryRepository

class ClearSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke() {
        repository.clearHistory()
    }
}
