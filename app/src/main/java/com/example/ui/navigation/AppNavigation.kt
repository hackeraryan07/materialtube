package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier 
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.search.SearchScreen
import com.example.ui.video.VideoPlayerScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "search" || currentRoute == "downloads") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == "search",
                        onClick = { navController.navigate("search") { launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Download, contentDescription = "Downloads") },
                        label = { Text("Downloads") },
                        selected = currentRoute == "downloads",
                        onClick = { navController.navigate("downloads") { launchSingleTop = true } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(onVideoClick = { url ->
                    navController.navigate("video/$url")
                })
            }
            composable("search") {
                SearchScreen(onVideoClick = { url ->
                    navController.navigate("video/$url")
                })
            }
            composable("downloads") {
                DownloadsScreen()
            }
            composable("video/{encodedUrl}") { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
                val url = try {
                    String(android.util.Base64.decode(encodedUrl, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
                } catch (e: Exception) {
                    java.net.URLDecoder.decode(encodedUrl, "UTF-8") // Fallback
                }
                VideoPlayerScreen(
                    videoUrl = url,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { clickedUrl ->
                        val nextEncoded = android.util.Base64.encodeToString(clickedUrl.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        navController.navigate("video/$nextEncoded")
                    }
                )
            }
        }
    }
}
