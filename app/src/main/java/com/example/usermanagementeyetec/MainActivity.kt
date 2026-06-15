package com.example.usermanagementeyetec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.usermanagementeyetec.ui.screens.UserFormScreen
import com.example.usermanagementeyetec.ui.screens.UserListScreen
import com.example.usermanagementeyetec.ui.theme.UserManagementEyetecTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserManagementEyetecTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "user_list"
                    ) {
                        composable("user_list") {
                            UserListScreen(
                                navController = navController,
                                padding = innerPadding
                            )
                        }
                        composable("user_form/{userId}") { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull()
                            UserFormScreen(
                                navController = navController,
                                userId = userId,
                                padding = innerPadding
                            )
                        }
                        composable("user_form") {
                            UserFormScreen(
                                navController = navController,
                                userId = null,
                                padding = innerPadding
                            )
                        }
                    }
                }
            }
        }
    }
}