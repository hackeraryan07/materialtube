package com.example.ui.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfo

class VideoPlayerViewModel : ViewModel() {
    private val repository = VideoRepository()

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Loading)
    val uiState: StateFlow<VideoUiState> = _uiState

    fun loadVideo(url: String) {
        viewModelScope.launch {
            _uiState.value = VideoUiState.Loading
            try {
                val info = repository.getStreamInfo(url)
                if (info == null) {
                    _uiState.value = VideoUiState.Error("Failed to fetch stream info (null).")
                } else {
                    val bestStream = info.videoStreams.maxByOrNull { it.getResolution().replace("p","").toIntOrNull() ?: 0 }
                    if (bestStream != null) {
                        _uiState.value = VideoUiState.Success(bestStream.content, info, bestStream.getResolution())
                    } else {
                        val audioOnly = info.audioStreams.firstOrNull()
                        if (audioOnly != null) {
                            _uiState.value = VideoUiState.Success(audioOnly.content, info, "Audio")
                        } else {
                            _uiState.value = VideoUiState.Error("No playable streams found.")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = VideoUiState.Error("Failed to load stream for: $url\n${e.message}")
            }
        }
    }

    fun changeQuality(streamUrl: String, qualityName: String) {
        val currentState = _uiState.value
        if (currentState is VideoUiState.Success) {
            _uiState.value = currentState.copy(streamUrl = streamUrl, selectedQualityName = qualityName)
        }
    }
}

sealed class VideoUiState {
    object Loading : VideoUiState()
    data class Success(
        val streamUrl: String,
        val info: StreamInfo,
        val selectedQualityName: String = ""
    ) : VideoUiState()
    data class Error(val message: String) : VideoUiState()
}
