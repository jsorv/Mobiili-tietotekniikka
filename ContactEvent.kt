package com.example.composetutorial

interface ContactEvent {
    object SaveContact: ContactEvent
    data class SetFirstName(val firstName: String): ContactEvent
    data class SetLastName(val lastName: String): ContactEvent
    data class SetPhoneNumber(val phoneNumber: String): ContactEvent
    data class SetImageUri(val imageUri: String) : ContactEvent
    object ShowDialog: ContactEvent
    object HideDialog: ContactEvent
    data class SortContacts(val sortType : SortType): ContactEvent
    data class DeleteContact(val contacts: Contacts): ContactEvent
}