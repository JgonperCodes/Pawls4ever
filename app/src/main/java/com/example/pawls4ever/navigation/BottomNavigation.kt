package com.example.pawls4ever.navigation
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        "Home" to Icons.Filled.Home,
        "My Pets" to Icons.Filled.Pets,
        "New Memory" to Icons.Filled.Create,
        "My Memories" to Icons.Filled.Book
    )

    //Para actualizar los colores con la ruta actual de pantallas
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = currentDestination?.route?.substringBefore("/")
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    NavigationBar(containerColor = Color(0xFFFFF4EC)) {
        items.forEachIndexed { index, item ->
            val route = when (index) {
                0 -> Screens.HomeScreen.name
                1 -> Screens.PetsScreen.name
                2 -> Screens.NewMemoryScreen.name
                3 -> Screens.AllMemorysScreen.name
                else -> ""
            }

            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(
                        when (index) {
                            0 -> Screens.HomeScreen.name
                            1 -> "${Screens.PetsScreen.name}/$userId"
                            2 -> "${Screens.NewMemoryScreen.name}/$userId"
                            3 -> "${Screens.AllMemorysScreen.name}/$userId"
                            else -> ""
                        }
                    ) {
                        popUpTo(Screens.HomeScreen.name) { inclusive = true }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        modifier = Modifier.size(30.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF6D213C),
                    unselectedIconColor = Color.LightGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}