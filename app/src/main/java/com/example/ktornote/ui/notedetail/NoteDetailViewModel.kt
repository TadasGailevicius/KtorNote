package com.example.ktornote.ui.notedetail

import androidx.lifecycle.ViewModel
import com.example.ktornote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    fun observeNoteById(noteID: String) = repository.observeNoteById(noteID)
}