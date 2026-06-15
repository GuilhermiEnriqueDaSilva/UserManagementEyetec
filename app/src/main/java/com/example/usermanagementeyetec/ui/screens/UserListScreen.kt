package com.example.usermanagementeyetec.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.usermanagementeyetec.ui.components.UserCard
import com.example.usermanagementeyetec.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    navController: NavController,
    padding: PaddingValues
) {
    // Obtém o Application a partir do contexto
    val application = LocalContext.current.applicationContext as Application
    val factory = remember { UserViewModel.Factory(application) }
    val viewModel: UserViewModel = viewModel(factory = factory)

    val allUsers by viewModel.allUsers.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuários") },
                actions = {
                    IconButton(onClick = { navController.navigate("user_form") }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(innerPadding)
        ) {
            if (allUsers.isEmpty()) {
                Text(
                    text = "Nenhum usuário cadastrado",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(allUsers) { user ->
                        UserCard(
                            user = user,
                            onEdit = {
                                navController.navigate("user_form/${user.id}")
                            },
                            onDelete = {
                                viewModel.deleteUser(user)
                            }
                        )
                    }
                }
            }
        }
    }
}