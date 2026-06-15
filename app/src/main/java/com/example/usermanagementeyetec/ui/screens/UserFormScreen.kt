package com.example.usermanagementeyetec.ui.screens

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.usermanagementeyetec.data.entity.User
import com.example.usermanagementeyetec.ui.viewmodel.UserViewModel
import com.example.usermanagementeyetec.utils.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    navController: NavController,
    userId: Long?,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = remember { UserViewModel.Factory(application) }
    val viewModel: UserViewModel = viewModel(factory = factory)

    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var existingPhotoPath by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Controle para mostrar a tela da câmera com rastreio
    var showCamera by remember { mutableStateOf(false) }

    // Carregar dados se for edição
    LaunchedEffect(userId) {
        if (userId != null && userId > 0) {
            val user = viewModel.getUserById(userId)
            user?.let {
                name = it.name
                email = it.email
                existingPhotoPath = it.photoUri
            }
        }
        isLoading = false
    }

    // Se a câmera estiver ativa, exibe a tela especial
    if (showCamera) {
        CameraScreen { imageUri ->
            if (imageUri != null) {
                // Salva a foto temporariamente em um arquivo (será movido no salvamento final)
                existingPhotoPath = imageUri.toString()
            }
            showCamera = false
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (userId == null) "Novo Usuário" else "Editar Usuário") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pré-visualização da foto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (existingPhotoPath != null) {
                            Image(
                                painter = rememberAsyncImagePainter(existingPhotoPath),
                                contentDescription = "Foto do usuário",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Sem foto",
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { showCamera = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tirar foto com rastreio facial")
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                var finalPhotoPath: String? = null

                                // Se existe uma foto temporária (URI)
                                if (existingPhotoPath != null && existingPhotoPath!!.startsWith("file://")) {
                                    val uri = Uri.parse(existingPhotoPath)
                                    if (userId == null) {
                                        // Novo usuário: insere primeiro, depois salva imagem com ID real
                                        val newId = viewModel.insertUserAndGetId(User(name = name, email = email))
                                        finalPhotoPath = ImageUtils.saveImageToInternalStorage(context, uri, newId)
                                        // Atualiza com a foto
                                        val updatedUser = User(id = newId, name = name, email = email, photoUri = finalPhotoPath)
                                        viewModel.updateUser(updatedUser)
                                    } else {
                                        // Edição: salva imagem com o ID existente
                                        finalPhotoPath = ImageUtils.saveImageToInternalStorage(context, uri, userId)
                                        val updatedUser = User(id = userId, name = name, email = email, photoUri = finalPhotoPath)
                                        viewModel.updateUser(updatedUser)
                                    }
                                } else {
                                    // Sem foto nova, apenas atualiza dados
                                    if (userId == null) {
                                        val newId = viewModel.insertUserAndGetId(User(name = name, email = email, photoUri = null))
                                        if (existingPhotoPath != null && !existingPhotoPath!!.startsWith("file://")) {
                                            // Caso já exista uma foto salva previamente (edição)
                                            finalPhotoPath = existingPhotoPath
                                            val updatedUser = User(id = newId, name = name, email = email, photoUri = finalPhotoPath)
                                            viewModel.updateUser(updatedUser)
                                        }
                                    } else {
                                        val updatedUser = User(id = userId, name = name, email = email, photoUri = existingPhotoPath)
                                        viewModel.updateUser(updatedUser)
                                    }
                                }

                                isSaving = false
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && email.isNotBlank() && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (userId == null) "Salvar" else "Atualizar")
                        }
                    }
                }
            }
        }
    }
}