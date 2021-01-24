package com.example.ktornote.data.remote.requests

data class AddOwnerRequest (
    val owner: String,
    val noteId: String
)