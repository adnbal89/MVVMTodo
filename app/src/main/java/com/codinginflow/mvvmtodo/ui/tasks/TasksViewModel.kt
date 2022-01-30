package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

//constructor --> ()
//inside viewmodels, we use @ViewModelInject, just the same as the inject.
//ViewModel never have to get reference from an activity or fragments, because generally viewModel lives longer than activity or fragments and the reference it holds
//may be long destroyed. --> memory leak.
//This is why we use FLow, it observes data.
//LiveData lifecycle aware.
//
class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    //like a mutable live data
    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesManager.preferencesFlow

    //channel instantiation. carries TasksEvent
    //if we expose a channel to the outside, then consumer fragment can put something in it. we do not want that.
    //to not to expose channel outside, we create tasksEvent val.
    private val tasksEventChannel = Channel<TasksEvent>()

    //basically turns this channel into a flow that we can than the fragment can use the single values out of it.
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    //Whenever searchQuery changes, execute this block //"it" is value of current query.
    //Using combine (inside FLow) to combine options above and apply for the data.
    //combine works like a dynamic filter. all of options combined.
    private val tasksFlow = combine(
        searchQuery,
        preferencesFlow

        //lambda expression.
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) {

    }

    //because update is a suspend function, so we need a coroutine
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    //actual delete functionality implemented here.
    //delete is a suspend funct. so we need a coroutine
    //viewModel is responsible for showing the snackBar (undo method),
    //only viewmodel knows when to show the snackbar but only the fragment or activity (context)
    //can show a snackbar, so this functionality is a bit tricky, beware of that.
    //viewModel never should contain any reference to a fragment or an activity, because of memory leak.

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        //we use kotlin channels, we can send data between two kotlin coroutines
        //channel instantiated above this class.
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    //to represent different kinds of events to be able to send to the fragment.
    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
    }
}

