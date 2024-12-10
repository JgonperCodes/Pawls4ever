package com.example.pawls4ever.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pawls4ever.screens.allMemorys.AllMemorysScreen
import com.example.pawls4ever.screens.splash.SplashScreen
import com.example.pawls4ever.screens.login.LoginScreen
import com.example.pawls4ever.screens.home.HomeScreen
import com.example.pawls4ever.screens.newMemory.NewMemoryScreen
import com.example.pawls4ever.screens.pets.PetsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: "" // Para conseguir el USERID muy importante

    NavHost(
        navController = navController,
        startDestination = Screens.SplashScreen.name
    ) {
        composable(Screens.SplashScreen.name) {
            SplashScreen(navController = navController)
        }
        composable(Screens.LoginScreen.name) {
            LoginScreen(navController = navController)
        }
        composable(Screens.HomeScreen.name) {
            HomeScreen(navController = navController)
        }

        //Para las pantallas que necesitan el user ID Ppara obtener los datos
        composable(
            route = "${Screens.PetsScreen.name}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userIdFromArgs = backStackEntry.arguments?.getString("userId") ?: ""
            PetsScreen(navController = navController, userId = userIdFromArgs)
        }
        composable(
            //Para declarar que estamos para crear una nueva nota o no
            route = "${Screens.NewMemoryScreen.name}/{userId}?isViewing={isViewing}&noteId={noteId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("isViewing") { type = NavType.BoolType; defaultValue = false },
                navArgument("noteId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val userIdFromArgs = backStackEntry.arguments?.getString("userId") ?: ""
            val isViewing = backStackEntry.arguments?.getBoolean("isViewing") ?: false
            val noteId = backStackEntry.arguments?.getString("noteId")
            NewMemoryScreen( //Declaramos sus propiedades
                navController = navController,
                userId = userIdFromArgs,
                isViewing = isViewing,
                noteId = noteId
            )
        }
        composable(
            route = "${Screens.AllMemorysScreen.name}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userIdFromArgs = backStackEntry.arguments?.getString("userId") ?: ""
            AllMemorysScreen(navController = navController, userId = userIdFromArgs)
        }
    }
}