package io.github.skydynamic.maidataviewer.ui

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

class AppNavController private constructor() {
    private var navController: NavHostController? = null

    fun initializeNavController(navController: NavHostController) {
        this.navController = navController
    }

    fun navigate(route: String) {
        if (this.navController != null) {
            this.navController!!.navigate(route)
        }
    }

    fun destroy() {
        this.navController = null
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AppNavController = AppNavController()

        fun getInstance(): AppNavController {
            return INSTANCE
        }

        fun destroy() {
            INSTANCE.destroy()
        }
    }
}