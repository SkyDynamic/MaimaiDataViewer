package io.github.skydynamic.maidataviewer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.page.AchievementDataTablePage
import io.github.skydynamic.maidataviewer.ui.page.HomePage
import io.github.skydynamic.maidataviewer.ui.page.MaimaiDataManagerPage
import io.github.skydynamic.maidataviewer.ui.page.MusicDetailPage
import io.github.skydynamic.maidataviewer.ui.page.MusicPage
import io.github.skydynamic.maidataviewer.ui.page.TreasureBoxPage
import io.github.skydynamic.maidataviewer.ui.page.treasurebox.RandomMusicPage
import io.github.skydynamic.maidataviewer.ui.page.treasurebox.RatingCalculatorPage
import io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection.CollectionPages
import io.github.skydynamic.maidataviewer.viewmodel.AchievementDataTablePageViewModel
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel

object AppContent {
    sealed class TabIcon {
        data class VectorIcon(val imageVector: ImageVector) : TabIcon()
        data class DrawableIcon(val resourceId: Int) : TabIcon()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    enum class Tab(
        val tabNameString: String,
        val icon: TabIcon,
        val content: (@Composable (
            jumpFunction: (Tab) -> Unit
        ) -> Unit) = {}
    ) {
        HOME(
            R.string.home.strings,
            TabIcon.VectorIcon(Icons.Filled.Home),
            { jumpFunction ->
                HomePage(
                    jumpFunction = jumpFunction
                )
            }
        ),
        Music(
            R.string.music_page.strings,
            TabIcon.DrawableIcon(R.drawable.music_fill),
            {
                MusicPage()
            }
        ),
        TreasureBox(
            R.string.treasure_box_page.strings,
            TabIcon.DrawableIcon(R.drawable.tbox),
            {
                TreasureBoxPage()
            }
        ),
//        SETTING(
//            R.string.setting_page.strings,
//            TabIcon.VectorIcon(Icons.Filled.Settings),
//            { _, _ ->
//                EmptyPage()
//            }
//        );
    }

    @Composable
    fun MainContent() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { item ->
                        val selected = GlobalViewModel.currentPage == item
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
                                GlobalViewModel.lastPage = GlobalViewModel.currentPage
                                GlobalViewModel.currentPage = item
                            },
                        )
                    }
                }
            }
        ) { insetsPadding ->
            GlobalViewModel.windowInsetsPadding = insetsPadding

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Bottom),
                            text = GlobalViewModel.currentPage.tabNameString,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = GlobalViewModel.currentPage,
                        transitionSpec = {
                            if (targetState.ordinal > GlobalViewModel.lastPage.ordinal) {
                                (slideInHorizontally { height -> height } + fadeIn()).togetherWith(
                                    slideOutHorizontally { height -> -height } + fadeOut())
                            } else {
                                (slideInHorizontally { height -> -height } + fadeIn()).togetherWith(
                                    slideOutHorizontally { height -> height } + fadeOut())
                            }
                        }
                    ) { targetState ->
                        targetState.content {
                            GlobalViewModel.lastPage = GlobalViewModel.currentPage
                            GlobalViewModel.currentPage = it
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Show() {
        val appNavController = AppNavController.getInstance()

        val navController = rememberNavController()

        DisposableEffect(appNavController) {
            appNavController.initializeNavController(navController)
            onDispose {
                AppNavController.destroy()
            }
        }

        NavHost(
            navController = navController,
            startDestination = "mainContent",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable("mainContent") {
                MainContent()
            }

            composable("dataManager") {
                MaimaiDataManagerPage {
                    navController.popBackStack()
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

            composable(
                "ratingCalculatorPage"
            ) {
                RatingCalculatorPage {
                    navController.popBackStack()
                }
            }

            composable(
                "randomMusicPage"
            ) {
                RandomMusicPage {
                    navController.popBackStack()
                }
            }

            CollectionPages.entries.forEach { page ->
                composable(
                    page.page + "?pickMode={pickMode}",
                    arguments = listOf(
                        navArgument("pickMode") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val pickMode = backStackEntry.arguments?.getBoolean("pickMode") ?: false
                    page.pageComposable(navController::popBackStack, pickMode)
                }
            }
        }
    }
}
