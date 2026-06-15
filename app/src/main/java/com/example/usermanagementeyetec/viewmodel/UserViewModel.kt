package com.example.usermanagementeyetec.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.usermanagementeyetec.data.database.AppDatabase
import com.example.usermanagementeyetec.data.entity.User
import com.example.usermanagementeyetec.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    val allUsers: Flow<List<User>>

    init {
        val userDao = AppDatabase.getInstance(application).userDao()
        repository = UserRepository(userDao)
        allUsers = repository.getAllUsers()
    }

    fun insertUser(user: User) = viewModelScope.launch {
        repository.insertUser(user)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        repository.updateUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        repository.deleteUser(user)
    }

    suspend fun insertUserAndGetId(user: User): Long = repository.insertUserAndGetId(user)

    suspend fun getUserById(id: Long): User? = repository.getUserById(id)


    // Factory correta para usar com viewModel()
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}