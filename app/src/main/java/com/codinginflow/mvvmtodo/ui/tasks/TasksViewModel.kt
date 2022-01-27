package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest

//constructor --> ()
//inside viewmodels, we use @ViewModelInject, just the same as the inject.
//ViewModel never have to get reference from an activity or fragments, because generally viewModel lives longer than activity or fragments and the reference it holds
//may be long destroyed. --> memory leak.
//This is why we use FLow, it observes data.
//LiveData lifecycle aware.
//
class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {
    //like a mutable live data
    val searchQuery = MutableStateFlow("")

    //Whenever searchQuery changes, execute this block //"it" is value of current query.
    private val tasksFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }

    val tasks = tasksFlow.asLiveData()
}