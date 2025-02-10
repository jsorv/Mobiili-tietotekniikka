package com.example.composetutorial

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contacts(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val imageUri: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
