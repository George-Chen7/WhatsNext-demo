package com.example.whatsnextdemo.ui.plan

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.databinding.ItemActionTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActionTaskAdapter(
    private val onToggleComplete: (ActionTaskEntity) -> Unit,
    private val onEdit: (ActionTaskEntity) -> Unit,
    private val onDelete: (ActionTaskEntity) -> Unit
) : RecyclerView.Adapter<ActionTaskAdapter.ActionTaskViewHolder>() {
    private val tasks: MutableList<ActionTaskEntity> = mutableListOf()
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionTaskViewHolder {
        val binding: ItemActionTaskBinding = ItemActionTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActionTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionTaskViewHolder, position: Int): Unit {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun submitList(newTasks: List<ActionTaskEntity>): Unit {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class ActionTaskViewHolder(
        private val binding: ItemActionTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: ActionTaskEntity): Unit {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskStatus.text = if (task.isCompleted) "已完成" else "进行中"
            binding.tvTaskTitle.paintFlags = if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            binding.tvTaskMeta.text = buildMetaText(task)
            binding.tvTaskNote.visibility = if (task.note.isBlank()) View.GONE else View.VISIBLE
            binding.tvTaskNote.text = task.note
            binding.btnToggleComplete.text = if (task.isCompleted) "恢复" else "完成"
            binding.btnToggleComplete.setOnClickListener { onToggleComplete(task) }
            binding.btnEditTask.setOnClickListener { onEdit(task) }
            binding.btnDeleteTask.setOnClickListener { onDelete(task) }
            binding.root.setOnClickListener { onEdit(task) }
        }

        private fun buildMetaText(task: ActionTaskEntity): String {
            val dueText: String = task.dueDate?.let { dueDate ->
                "截止 ${dateFormatter.format(Date(dueDate))}"
            } ?: "无截止日期"
            val priorityText: String = "${ActionTaskValues.priorityLabel(task.priority)}优先级"
            val sourceText: String = ActionTaskValues.sourceLabel(task.source)
            return "${task.category} · $priorityText · $dueText · $sourceText"
        }
    }
}
