package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao

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

    val tasks = taskDao.getTasks().asLiveData()
}