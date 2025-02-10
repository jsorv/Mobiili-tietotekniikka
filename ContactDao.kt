package com.example.composetutorial

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Upsert
    suspend fun upsertContact(contacts: Contacts)

    @Delete
    suspend fun deleteContact(contacts: Contacts)

    @Query("SELECT * FROM contacts ORDER BY firstName ASC")
    fun getContactsOrderedByFirstName(): Flow<List<Contacts>>

    @Query("SELECT * FROM contacts ORDER BY lastName ASC")
    fun getContactsOrderedByLastName(): Flow<List<Contacts>>

    @Query("SELECT * FROM contacts ORDER BY phoneNumber ASC")
    fun getContactsOrderedByPhoneNumber(): Flow<List<Contacts>>
}