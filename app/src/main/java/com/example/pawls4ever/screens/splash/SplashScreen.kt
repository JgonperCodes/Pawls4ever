package com.example.pawls4ever.screens.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawls4ever.R
import com.example.pawls4ever.navigation.Screens
import com.example.pawls4ever.ui.theme.Roboto
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Animaciones y valores para guardar
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(-340f) }
    val textAlpha = remember { Animatable(0f) }
    val pawOffsetTopLeft = remember { Animatable(-200f) }
    val pawOffsetBottomRight = remember { Animatable(200f) }
    val pawRotationTopLeft = remember { Animatable(0f) }
    val pawRotationBottomRight = remember { Animatable(0f) }

    //Aignamos cada valor de la animación sus tiempos
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(4f).getInterpolation(it) }
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        pawOffsetTopLeft.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = { OvershootInterpolator(2f).getInterpolation(it) })
        )
        pawRotationTopLeft.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 800)
        )

        pawOffsetBottomRight.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = { OvershootInterpolator(2f).getInterpolation(it) })
        )
        pawRotationBottomRight.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 800)
        )

        delay(200)

        textOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = { OvershootInterpolator(2f).getInterpolation(it) })
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        delay(1400)

        //Para acceder despues de la animación al Login si no hay usuario con sesion iniciada o home
        if (FirebaseAuth.getInstance().currentUser?.email.isNullOrEmpty()) {
            navController.navigate(Screens.LoginScreen.name) {
                popUpTo(Screens.SplashScreen.name) { inclusive = true }
            }
        } else {
            navController.navigate(Screens.HomeScreen.name) {
                popUpTo(Screens.SplashScreen.name) { inclusive = true }
            }
        }
    }

    //Surface que contiene los elementos de la SplasScreen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF4EC)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            //Patas que luego giran
            Image(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = "Pata esquina superior izquierda",
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = pawOffsetTopLeft.value.dp, y = -10.dp)
                    .rotate(pawRotationTopLeft.value)
                    .align(Alignment.TopStart)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = "Pata esquina inferior derecha",
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = pawOffsetBottomRight.value.dp, y = -pawOffsetBottomRight.value.dp)
                    .rotate(pawRotationBottomRight.value)
                    .align(Alignment.BottomEnd)
            )
            // Logo central
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Letras Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.iconoapp),
                    contentDescription = "Logo de Pawls4Ever",
                    modifier = Modifier
                        .size(230.dp)
                        .scale(scale.value)
                        .alpha(alpha.value)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .offset(x = textOffset.value.dp, y = 4.dp)
                        .alpha(textAlpha.value)
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    //Texto que luego aparece difuminandose
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier
                            .offset(y = -4.dp)
                    ) {
                        Text(
                            text = "Registra y ten las memorias de tu mejor amigo siempre a tu lado",
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                            ),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cuadrado de sombra del texto
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .offset(x = textOffset.value.dp)
                        .alpha(textAlpha.value)
                ) {
                }
            }
        }
    }
}


