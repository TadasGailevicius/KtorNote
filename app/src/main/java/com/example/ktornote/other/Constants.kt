package com.example.ktornote.other

object Constants {

    const val DATABASE_NAME = "notes_db"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val BASE_URL = "http://192.168.8.103:8001"

    val IGNORE_AUTH_URLS = listOf("/login", "register")
}