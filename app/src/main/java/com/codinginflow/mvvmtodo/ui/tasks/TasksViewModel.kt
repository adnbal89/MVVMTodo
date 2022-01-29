package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()
}

