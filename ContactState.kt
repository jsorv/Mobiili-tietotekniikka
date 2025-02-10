package com.example.composetutorial

data class ContactState(
    val contacts: List<Contacts> = emptyList(),
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isAddingContact: Boolean = false,
    val sortType: SortType = SortType.FIRST_NAME,
    val imageUri: String? = null
)
