package com.example.usermanagementeyetec.data.repository

import com.example.usermanagementeyetec.data.dao.UserDao
import com.example.usermanagementeyetec.data.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)

    suspend fun insertUser(user: User) = userDao.insert(user)

    suspend fun updateUser(user: User) = userDao.update(user)

    suspend fun deleteUser(user: User) = userDao.delete(user)
}