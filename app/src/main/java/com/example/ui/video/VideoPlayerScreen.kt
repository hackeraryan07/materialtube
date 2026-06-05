package com.example.ui.video

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.ui.home.VideoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit = {},
    viewModel: VideoPlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(videoUrl) {
        viewModel.loadVideo(videoUrl)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is VideoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VideoUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is VideoUiState.Success -> {
                    VideoPlayer(
                        streamUrl = state.streamUrl,
                        onSettingsClick = { showSettingsSheet = true }
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.info.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            val views = if (state.info.viewCount >= 0) "${state.info.viewCount} views" else ""
                            val date = state.info.uploadDate?.toString() ?: ""
                            Text(
                                text = "$views • $date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            // Channel Info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatarUrl = state.info.uploaderAvatars.firstOrNull()?.url ?: ""
                                GlideImage(
                                    model = avatarUrl,
                                    contentDescription = "Channel Avatar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.info.uploaderName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val subCount = state.info.uploaderSubscriberCount
                                    if (subCount > 0) {
                                        Text(
                                            text = "$subCount subscribers",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = { },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface,
                                        contentColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text("Subscribe")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            // Actions Row
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        onClick = {}
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ThumbUp, contentDescription = "Like", modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(if(state.info.likeCount > 0) state.info.likeCount.toString() else "Like", style = MaterialTheme.typography.labelLarge)
                                            Spacer(Modifier.width(12.dp))
                                            HorizontalDivider(modifier = Modifier.width(1.dp).height(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(12.dp))
                                            Icon(Icons.Outlined.ThumbDown, contentDescription = "Dislike", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                item {
                                    ActionButton(Icons.Default.Share, "Share")
                                }
                                item {
                                    ActionButton(Icons.Default.Download, "Download")
                                }
                                item {
                                    ActionButton(Icons.AutoMirrored.Filled.PlaylistAdd, "Save")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            // Description Container
                            var expanded by remember { mutableStateOf(false) }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                            ) {
                                val descText = state.info.description?.content ?: ""
                                Text(
                                    text = if (expanded) descText else descText.take(150) + if (descText.length > 150) "..." else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Related Items
                        val related = state.info.relatedItems ?: emptyList()
                        items(related.size) { index ->
                            val relatedItem = related[index]
                            if (relatedItem is StreamInfoItem) {
                                VideoItem(item = relatedItem, onClick = { onVideoClick(relatedItem.url) })
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showSettingsSheet && uiState is VideoUiState.Success) {
        val state = uiState as VideoUiState.Success
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Quality for Video", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(state.info.videoStreams.size) { index ->
                        val stream = state.info.videoStreams[index]
                        val resolution = stream.getResolution()
                        val isSelected = resolution == state.selectedQualityName
                        ListItem(
                            headlineContent = { Text(resolution) },
                            trailingContent = {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected")
                            },
                            modifier = Modifier.clickable {
                                viewModel.changeQuality(stream.content, resolution)
                                showSettingsSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = {}
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun VideoPlayer(streamUrl: String, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    var isControllerVisible by remember { mutableStateOf(true) }
    var isFirstLaunch by remember { mutableStateOf(true) }
    
    val exoPlayer = remember {
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    LaunchedEffect(streamUrl) {
        val playbackPosition = exoPlayer.currentPosition
        val playWhenReadyState = if (isFirstLaunch) true else exoPlayer.playWhenReady
        isFirstLaunch = false
        
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        exoPlayer.setMediaItem(mediaItem)
        if (playbackPosition > 0L) {
            exoPlayer.seekTo(playbackPosition)
        }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReadyState
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                        isControllerVisible = (visibility == android.view.View.VISIBLE)
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        androidx.compose.animation.AnimatedVisibility(
            visible = isControllerVisible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}
