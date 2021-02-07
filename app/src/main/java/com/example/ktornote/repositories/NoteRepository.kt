package com.example.ktornote.repositories

import android.app.Application
import com.example.ktornote.data.local.NoteDao
import com.example.ktornote.data.remote.NoteApi
import com.example.ktornote.data.remote.requests.AccountRequest
import com.example.ktornote.other.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class NoteRepository @Inject constructor(
        private val noteDao: NoteDao,
        private val noteApi: NoteApi,
        private val context: Application
) {
    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch(e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }
}