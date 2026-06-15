package com.example.usermanagementeyetec.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.usermanagementeyetec.data.entity.User
import com.example.usermanagementeyetec.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    navController: NavController,
    userId: Long?,
    padding: PaddingValues
) {
    val application = LocalContext.current.applicationContext as Application
    val factory = remember { UserViewModel.Factory(application) }
    val viewModel: UserViewModel = viewModel(factory = factory)

    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Carregar dados se for edição
    LaunchedEffect(userId) {
        if (userId != null) {
            val user = viewModel.getUserById(userId)
            user?.let {
                name = it.name
                email = it.email
                photoUri = it.photoUri
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
                // Placeholder para foto
                Text(
                    text = "Funcionalidade de foto será implementada depois",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = {
                        scope.launch {
                            val user = User(
                                id = userId ?: 0,
                                name = name,
                                email = email,
                                photoUri = photoUri
                            )
                            if (userId == null) {
                                viewModel.insertUser(user)
                            } else {
                                viewModel.updateUser(user)
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && email.isNotBlank()
                ) {
                    Text(if (userId == null) "Salvar" else "Atualizar")
                }
            }
        }
    }
}