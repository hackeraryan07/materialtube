package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class SearchViewModel : ViewModel() {
    private val repository = VideoRepository()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val items = repository.searchVideos(query)
            if (items.isEmpty()) {
                _uiState.value = SearchUiState.Error("No results found for '$query'")
            } else {
                _uiState.value = SearchUiState.Success(items)
            }
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val items: List<StreamInfoItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
