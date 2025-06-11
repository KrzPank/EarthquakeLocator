package com.example.earthquakelocator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.earthquakelocator.view.EarthquakeFormScreen
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.tooling.preview.Preview
import com.example.earthquakelocator.ui.theme.EarthquakeTheme
import com.example.earthquakelocator.view.MainMenuScreen
import com.example.earthquakelocator.view.QuickSearchScreen

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EarthquakeTheme{
                AppNavigation()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "MainMenuScreen"
    ) {
        composable("MainMenuScreen") {
            MainMenuScreen(navController)
        }
        composable("EarthquakeFormScreen") {
            EarthquakeFormScreen(navController)
        }
        composable("QuickSearchScreen") {
            QuickSearchScreen(navController)
        }
    }

}

