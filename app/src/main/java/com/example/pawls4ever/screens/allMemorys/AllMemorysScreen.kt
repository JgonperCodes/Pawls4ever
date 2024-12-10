package com.example.pawls4ever.screens.allMemorys
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.navigation.BottomNavigationBar
import com.example.pawls4ever.navigation.Screens
import com.example.pawls4ever.ui.theme.Roboto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMemorysScreen(navController: NavController, userId: String) {
    val viewModel: AllMemorysScreenViewModel = viewModel()
    val notes by viewModel.notes.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    //  Para cragar los datos inicialmente
    LaunchedEffect(userId) {
        viewModel.fetchNotes(userId)
        viewModel.fetchPets(userId)
    }

    //Con LaunchedEffect le mando a traves del boolean de NewMemoryScreen un indicador para refrescar la pantalla y obtener de nuevo las notas
    LaunchedEffect(navController.currentBackStackEntry?.savedStateHandle) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("refreshHome")?.observeForever { shouldRefreshHome ->
            if (shouldRefreshHome == true) {
                viewModel.fetchNotes(userId)
                savedStateHandle.set("refreshHome", false)
            }
        }
        savedStateHandle?.getLiveData<Boolean>("refreshAllMemories")
            ?.observeForever { shouldRefreshAll ->
                if (shouldRefreshAll == true) {
                    viewModel.fetchNotes(userId)
                    savedStateHandle.set("refreshAllMemories", false)
                }
            }
    }

    //TOP APPBAR Y BOTTOM AP BAR
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Todos los recuerdos",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF4EC)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        //Box para agrupar los elementos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD6A8))
                .padding(padding)
        ) {
            //Columna para agrupar todas las entradas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!isLoading) {
                    // Seccion: Ultimas entradas
                    if (notes.isNotEmpty()) {
                        TubeText2(
                            text = "Últimas entradas",
                            textColor = Color.White,
                            backgroundColor = Color(0xFFC98686)
                        )
                        LazyRow {
                            items(notes.size) { index ->
                                val note = notes[index]
                                MemoryCard(note = note) {
                                    navController.navigate(
                                        "${Screens.NewMemoryScreen.name}/$userId?isViewing=true&noteId=${note.noteId}"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    //  Seccion: Favoritos
                    val favoriteNotes = notes.filter { it.favorite }
                    if (favoriteNotes.isNotEmpty()) {
                        TubeText2(
                            text = "Entradas Importantes",
                            textColor = Color.White,
                            backgroundColor = Color(0xFFC98686)
                        )
                        LazyRow {
                            items(favoriteNotes.size) { index ->
                                val note = favoriteNotes[index]
                                MemoryCard(note = note) {
                                    navController.navigate(
                                        "${Screens.NewMemoryScreen.name}/$userId?isViewing=true&noteId=${note.noteId}"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Seccion que crea y agrupa las notas si tienen etiquetados alguna mascota
                    pets.forEach { pet ->
                        val petNotes = notes.filter {
                            it.petTag.any { tag -> tag.path.endsWith(pet.petId) }
                        }
                        if (petNotes.isNotEmpty()) {
                            TubeText2(
                                text = "Entradas de ${pet.name}",
                                textColor = Color.White,
                                backgroundColor = Color(0xFFC98686)
                            )
                            LazyRow { //Obtenemos la lista del user de pets y le asignamos su ruta con el id de la nota
                                items(petNotes.size) { index ->
                                    val note = petNotes[index]
                                    MemoryCard(note = note) {
                                        navController.navigate(
                                            "${Screens.NewMemoryScreen.name}/$userId?isViewing=true&noteId=${note.noteId}"
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Loading global para que se recarge las pantallas sin que el usuario lo vea
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable(enabled = false) {}, // Paramos las interacciones con está linea
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}

//TubeText para darle estilo a las etiquetas, es el 2 porque cambia cosas del original como tamaños y fonts
@Composable
fun TubeText2(text: String, textColor: Color, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        )
    }
}

//Funcion para crear las cards que vemos de memorias, agrupan las notas y las muestra con los datos basicos de estas
@Composable
fun MemoryCard(note: Note, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Carga la imagen de la bd con el link
            AsyncImage(
                model = note.images.firstOrNull() ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(5.dp),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(note.date ?: Date()),
                style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = note.title,
                style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}