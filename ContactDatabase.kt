package com.example.composetutorial

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.composetutorial.ContactDao

@Database(
    entities = [Contacts::class],
    version = 2,
    exportSchema = false
)

abstract class ContactDatabase: RoomDatabase() {

    abstract val dao: ContactDao
}