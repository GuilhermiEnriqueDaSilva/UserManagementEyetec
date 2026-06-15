package com.example.usermanagementeyetec.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val photoUri: String? = null  // armazenará o caminho absoluto da imagem
)