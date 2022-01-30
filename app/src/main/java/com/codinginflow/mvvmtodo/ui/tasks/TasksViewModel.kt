package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
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
// we are going to save searchQuery in savedState
class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    //like a mutable live data
    val searchQuery = state.getLiveData("searchQuery", "")

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
        searchQuery.asFlow(),
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

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
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

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    //to represent different kinds of events to be able to send to the fragment.
    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
    }
}

