package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class HomeViewModel : ViewModel() {
    private val repository = VideoRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        fetchTrending()
    }

    private fun fetchTrending() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val items = repository.getTrendingVideos()
            if (items.isEmpty()) {
                _uiState.value = HomeUiState.Error("Failed to fetch trending videos.")
            } else {
                _uiState.value = HomeUiState.Success(items)
            }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val items: List<StreamInfoItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
