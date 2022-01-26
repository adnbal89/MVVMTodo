package com.codinginflow.mvvmtodo.ui.tasks

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

//fragments and activities cannot be constructor injected. So we use @AndroidEntryPoint
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    //we declare our viewmodel as this
    // viewModels : Property Delegate
    //this also injected by dagger, as long as we have @AndroidEntryPoint
    private val viewModel: TasksViewModel by viewModels()
}