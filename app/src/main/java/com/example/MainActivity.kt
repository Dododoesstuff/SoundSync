package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MigrationScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SoundSyncTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val accentTheme by viewModel.accentTheme.collectAsStateWithLifecycle()

            SoundSyncTheme(
                themeMode = themeMode,
                accentTheme = accentTheme
            ) {
                SoundSyncApp(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun SoundSyncApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem(AppScreen.DASHBOARD, "Home", Icons.Default.Home, "nav_dashboard"),
        NavItem(AppScreen.SEARCH, "Search", Icons.Default.Search, "nav_search"),
        NavItem(AppScreen.LIBRARY, "Library", Icons.Default.LibraryMusic, "nav_library"),
        NavItem(AppScreen.SETTINGS, "Settings", Icons.Default.Settings, "nav_settings")
    )

    // Handle back button when full player is open
    BackHandler(enabled = isPlayerExpanded) {
        viewModel.setPlayerExpanded(false)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Desktop / Tablet layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentScreen == item.screen,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(screen = currentScreen, viewModel = viewModel)

                    // Docked bottom Mini Player on wide screen
                    MiniPlayerBar(
                        playbackState = playbackState,
                        onExpand = { viewModel.setPlayerExpanded(true) },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.9f)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        } else {
            // Mobile Compact Layout with Bottom NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Mini Player right above Bottom Navigation
                        MiniPlayerBar(
                            playbackState = playbackState,
                            onExpand = { viewModel.setPlayerExpanded(true) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextTrack() }
                        )

                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentScreen == item.screen,
                                    onClick = { viewModel.navigateTo(item.screen) },
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(screen = currentScreen, viewModel = viewModel)
                }
            }
        }

        // Full Screen Player Modal / Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun ScreenContent(screen: AppScreen, viewModel: MainViewModel) {
    when (screen) {
        AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        AppScreen.SEARCH -> SearchScreen(viewModel = viewModel)
        AppScreen.LIBRARY -> LibraryScreen(viewModel = viewModel)
        AppScreen.MIGRATION -> MigrationScreen(viewModel = viewModel)
        AppScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
        AppScreen.SOCIAL -> SocialScreen(viewModel = viewModel)
        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
    }
}

