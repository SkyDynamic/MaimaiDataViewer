package io.github.skydynamic.maidataviewer.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.skydynamic.maidataviewer.MainActivity
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer
import io.github.skydynamic.maidataviewer.ui.page.AchievementDataTablePage
import io.github.skydynamic.maidataviewer.ui.page.HomePage
import io.github.skydynamic.maidataviewer.ui.page.MusicDetailPage
import io.github.skydynamic.maidataviewer.ui.page.MusicPage
import io.github.skydynamic.maidataviewer.ui.page.EmptyPage
import io.github.skydynamic.maidataviewer.viewmodel.AchievementDataTablePageViewModel
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel

object AppContent {
    sealed class TabIcon {
        data class VectorIcon(val imageVector: ImageVector) : TabIcon()
        data class DrawableIcon(val resourceId: Int) : TabIcon()
    }

    enum class Tab(
        val tabNameString: String,
        val icon: TabIcon
    ) {
        HOME(
            R.string.home.getString(),
            TabIcon.VectorIcon(Icons.Filled.Home)
        ),
        Music(
            R.string.music_page.getString(),
            TabIcon.DrawableIcon(R.drawable.music_fill)
        ),
        SETTING(
            R.string.setting_page.getString(),
            TabIcon.VectorIcon(Icons.Filled.Settings)
        );
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun MainContent(
        sharedTransitionScope: SharedTransitionScope,
        animatedContentScope: AnimatedContentScope,
        currentPage: Tab,
        onNavigate: (String) -> Unit,
        onCardClick: (MaimaiMusicData) -> Unit
    ) {
        var currentTabName by remember { mutableStateOf(currentPage.tabNameString) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { item ->
                        val selected = currentTabName == item.tabNameString
                        NavigationBarItem(
                            icon = {
                                val icon = item.icon
                                when (icon) {
                                    is TabIcon.VectorIcon -> Icon(
                                        icon.imageVector,
                                        contentDescription = item.tabNameString,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    is TabIcon.DrawableIcon -> Icon(
                                        painter = painterResource(icon.resourceId),
                                        contentDescription = item.tabNameString,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = { Text(item.tabNameString) },
                            selected = selected,
                            onClick = {
                                onNavigate(item.tabNameString)
                                currentTabName = item.tabNameString
                            },
                        )
                    }
                }
            }
        ) { insetsPadding ->
            GlobalViewModel.windowInsetsPadding = insetsPadding

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .padding(
                            start = WindowInsetsSpacer.startPadding,
                            end = WindowInsetsSpacer.endPadding
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Bottom),
                            text = currentTabName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentPage) {
                        Tab.HOME -> HomePage { onNavigate(it.tabNameString) }
                        Tab.Music -> MusicPage(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                            onCardClick = onCardClick
                        )

                        Tab.SETTING -> EmptyPage()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun Show() {
        SharedTransitionLayout {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Tab.HOME.tabNameString,
                modifier = Modifier.fillMaxSize()
            ) {
                Tab.entries.forEach { tab ->
                    composable(tab.tabNameString) {
                        MainContent(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            currentPage = tab,
                            onNavigate = { destination ->
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onCardClick = { musicData ->
                                navController.navigate("musicDetail/${musicData.id}")
                            }
                        )
                    }
                }

                composable(
                    "musicDetail/{id}",
                    arguments = listOf(
                        navArgument("id") {
                            type = NavType.IntType
                        }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                    val musicData = MusicDataManager.getMusicData(id)
                    musicData?.let {
                        MusicDetailPage(
                            it,
                            this@SharedTransitionLayout,
                            this@composable,
                            onBackPressed = {
                                navController.popBackStack()
                            }
                        ) { columns, rows ->
                            AchievementDataTablePageViewModel.columns = columns
                            AchievementDataTablePageViewModel.rows = rows
                            MainActivity.rotationScreen()
                            navController.navigate("achievementDataTable")
                        }
                    } ?: run {
                        navController.popBackStack()
                    }
                }

                composable(
                    "achievementDataTable"
                ) {
                    AchievementDataTablePage(
                        columns = AchievementDataTablePageViewModel.columns,
                        rows = AchievementDataTablePageViewModel.rows
                    ) {
                        MainActivity.rotationScreen(true)
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
