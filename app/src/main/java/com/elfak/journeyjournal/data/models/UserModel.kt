package com.elfak.journeyjournal.data.models

data class UserModel(
    var username: String = "",
    var email: String = "",
    var dateOfBirth: String = "",
    var imageURL: String = "",
    var friendsList: List<String> = emptyList()
)
