package com.example.ktornote.repositories

import android.app.Application
import com.example.ktornote.data.local.NoteDao
import com.example.ktornote.data.local.entities.LocallyDeletedNoteID
import com.example.ktornote.data.local.entities.Note
import com.example.ktornote.data.remote.NoteApi
import com.example.ktornote.data.remote.requests.AccountRequest
import com.example.ktornote.data.remote.requests.DeleteNoteRequest
import com.example.ktornote.other.Resource
import com.example.ktornote.other.checkForInternetConnection
import com.example.ktornote.other.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

class NoteRepository @Inject constructor(
        private val noteDao: NoteDao,
        private val noteApi: NoteApi,
        private val context: Application
) {

    suspend fun insertNote(note: Note) {
        val response = try {
            noteApi.addNote(note)
        } catch (e: Exception) {
            null
        }

        if(response != null && response.isSuccessful){
            noteDao.insertNote(note.apply { isSynced = true })
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun insertNotes(notes: List<Note>){
        notes.forEach { insertNote(it) }
    }

    suspend fun deleteNote(noteID: String) {
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteID))
        } catch (e: Exception) {
            null
        }

        noteDao.deleteNoteById(noteID)

        if(response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeleteNoteId(LocallyDeletedNoteID(noteID))
        } else {
            deleteLocallyDeletedNoteID(noteID)
        }

    }

    suspend fun deleteLocallyDeletedNoteID(deletedNoteID: String) {
        noteDao.deleteLocallyDeletedNoteID(deletedNoteID)
    }

    suspend fun getNoteById(noteID: String) = noteDao.getNoteById(noteID)

    private var curNotesResponse: Response<List<Note>>? = null

    suspend fun synchNotes() {
        val locallyDeletedNoteIDs = noteDao.getAllLocallyDeletedNotes()
        locallyDeletedNoteIDs.forEach { id -> deleteNote(id.deletedNoteID) }

        val unsyncedNotes = noteDao.getAllUnsyncedNotes()
        unsyncedNotes.forEach { note -> insertNote(note) }

        curNotesResponse = noteApi.getNotes()
        curNotesResponse?.body()?.let { notes ->
            noteDao.deleteAllNotes()
            insertNotes(notes.onEach { note -> note.isSynced = true })
        }
    }

    fun getAllNotes(): Flow<Resource<List<Note>>> {
        return networkBoundResource(
                query = {
                    noteDao.getAllNotes()
                },
                fetch = {
                    synchNotes()
                    curNotesResponse
                },
                saveFetchResult = { response ->
                    response?.body()?.let {
                        insertNotes(it.onEach { note -> note.isSynced = true })
                    }
                },
                shouldFetch = {
                    checkForInternetConnection(context)
                }
        )
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.login(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch(e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }

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