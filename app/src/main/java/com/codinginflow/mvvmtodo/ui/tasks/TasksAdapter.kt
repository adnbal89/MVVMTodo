package com.codinginflow.mvvmtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.ItemTaskBinding

class TasksAdapter : ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    //how to instantiate one of our viewHolder classes.
    //Whenever an item in the list needed, this is how it can get one.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {

        //layoutinflation means xml to object
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    //we have to define how we bind the data to the viewHolder
    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    //binding from XML, you can use inside the function.
    class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        //When one of Task's fields have changed, this callback will know. and we know that we have to refresh item on the screen
        //= ->  return x functionality is the same.
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem

    }

}