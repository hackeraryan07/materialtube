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
                    val bestAudio = info.audioStreams.maxByOrNull { it.getBitrate() }
                    
                    val qualityOptions = mutableListOf<QualityOption>()
                    
                    info.videoStreams.forEach { stream ->
                        qualityOptions.add(QualityOption(stream.getResolution(), stream.content, null))
                    }
                    
                    info.videoOnlyStreams.forEach { stream ->
                        val resolution = stream.getResolution()
                        if (bestAudio != null) {
                            qualityOptions.add(QualityOption(resolution, stream.content, bestAudio.content))
                        }
                    }
                    
                    val optionsDistinct = qualityOptions.distinctBy { it.resolution }.sortedByDescending { it.resolution.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
                    
                    if (optionsDistinct.isNotEmpty()) {
                        val selected = optionsDistinct.first()
                        _uiState.value = VideoUiState.Success(
                            streamUrl = selected.videoUrl,
                            audioUrl = selected.audioUrl,
                            info = info,
                            selectedQualityName = selected.resolution,
                            qualityOptions = optionsDistinct
                        )
                    } else if (bestAudio != null) {
                        _uiState.value = VideoUiState.Success(
                            streamUrl = bestAudio.content,
                            audioUrl = null,
                            info = info,
                            selectedQualityName = "Audio Only",
                            qualityOptions = listOf(QualityOption("Audio Only", bestAudio.content, null))
                        )
                    } else {
                        _uiState.value = VideoUiState.Error("No playable streams found.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = VideoUiState.Error("Failed to load stream for: $url\n${e.message}")
            }
        }
    }

    fun changeQuality(streamUrl: String, audioUrl: String?, qualityName: String) {
        val currentState = _uiState.value
        if (currentState is VideoUiState.Success) {
            _uiState.value = currentState.copy(streamUrl = streamUrl, audioUrl = audioUrl, selectedQualityName = qualityName)
        }
    }
}

data class QualityOption(
    val resolution: String,
    val videoUrl: String,
    val audioUrl: String? = null
)

sealed class VideoUiState {
    object Loading : VideoUiState()
    data class Success(
        val streamUrl: String,
        val audioUrl: String?,
        val info: StreamInfo,
        val selectedQualityName: String = "",
        val qualityOptions: List<QualityOption> = emptyList()
    ) : VideoUiState()
    data class Error(val message: String) : VideoUiState()
}
