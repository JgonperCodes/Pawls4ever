package com.example.pawls4ever

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.pawls4ever.navigation.Navigation
import com.example.pawls4ever.ui.theme.Pawls4everTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "Firebase inicializado correctamente")
        } catch (e: FirebaseException) {
            Log.e("MainActivity", "Firebase ha fallado", e)
        }
        setContent {
            Pawls4everTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Navigation()
                    }
                }
            }
        }
    }
}