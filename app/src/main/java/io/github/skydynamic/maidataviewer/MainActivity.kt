package io.github.skydynamic.maidataviewer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.skydynamic.maidataviewer.ui.AppContent
import io.github.skydynamic.maidataviewer.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppContent.Show()
            }
        }
        MainActivity = this
    }

    companion object {
        lateinit var MainActivity: MainActivity

        fun rotationScreen(reverse: Boolean = false) {
            if (reverse) {
                MainActivity.runOnUiThread {
                    MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            } else {
                MainActivity.runOnUiThread {
                    MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }
}
