package com.example.usermanagementeyetec.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.usermanagementeyetec.data.entity.User
import com.example.usermanagementeyetec.ui.viewmodel.UserViewModel
import com.example.usermanagementeyetec.utils.ImageUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Função para criar arquivo temporário
    fun createTempImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.filesDir
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            tempPhotoFile = this
        }
    }

    // Launcher para tirar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                existingPhotoPath = file.absolutePath
            }
        }
    }

    // Launcher para pedir permissão da câmera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = createTempImageFile()
            file?.let {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                cameraLauncher.launch(uri)
            }
        }
    }

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
                    onClick = {
                        when {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                                val file = createTempImageFile()
                                file?.let {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                                    cameraLauncher.launch(uri)
                                }
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tirar foto")
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
                            if (userId == null) {
                                // Novo usuário
                                val newId = viewModel.insertUserAndGetId(User(name = name, email = email))
                                var finalPhotoPath: String? = null
                                if (tempPhotoFile?.exists() == true) {
                                    finalPhotoPath = ImageUtils.saveImageToInternalStorage(
                                        context,
                                        Uri.fromFile(tempPhotoFile!!),
                                        newId
                                    )
                                    tempPhotoFile?.delete()
                                }
                                val updatedUser = User(id = newId, name = name, email = email, photoUri = finalPhotoPath)
                                if (finalPhotoPath != null) {
                                    viewModel.updateUser(updatedUser)
                                }
                            } else {
                                // Edição
                                var finalPhotoPath = existingPhotoPath
                                if (tempPhotoFile?.exists() == true) {
                                    existingPhotoPath?.let { ImageUtils.deleteImageFromInternalStorage(context, it) }
                                    finalPhotoPath = ImageUtils.saveImageToInternalStorage(
                                        context,
                                        Uri.fromFile(tempPhotoFile!!),
                                        userId
                                    )
                                    tempPhotoFile?.delete()
                                }
                                val updatedUser = User(id = userId, name = name, email = email, photoUri = finalPhotoPath)
                                viewModel.updateUser(updatedUser)
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