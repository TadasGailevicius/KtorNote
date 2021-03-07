package com.example.ktornote.ui.notes

import androidx.lifecycle.*
import com.example.ktornote.data.local.entities.LocallyDeletedNoteID
import com.example.ktornote.data.local.entities.Note
import com.example.ktornote.other.Event
import com.example.ktornote.other.Resource
import com.example.ktornote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
        private val repository: NoteRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _allNotes = _forceUpdate.switchMap {
        repository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }

    val allNotes: LiveData<Event<Resource<List<Note>>>> = _allNotes

    fun synchAllNotes() = _forceUpdate.postValue(true)

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(noteID: String) = viewModelScope.launch {
        repository.deleteNote(noteID)
    }

    fun deleteLocallyDeletedNoteID(deletedNoteID: String) = viewModelScope.launch {
        repository.deleteLocallyDeletedNoteID(deletedNoteID)
    }


}