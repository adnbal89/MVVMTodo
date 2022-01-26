package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint

//fragments and activities cannot be constructor injected. So we use @AndroidEntryPoint

//inflate() not needed, because here, it is already done.
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    //we declare our viewmodel as this
    // viewModels : Property Delegate
    //this also injected by dagger, as long as we have @AndroidEntryPoint
    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val taskAdapter = TasksAdapter()

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                //do not do db operations inside the fragment.
                //The fragment doesn't need to hold data -> separation of concerns.
                //for example, when the screen rotated or fragment destroyed, the data is lost and need to be recreated. (leads to memory leak)
                //viewModel will be in charge to manage data for fragment.


            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }
    }
}