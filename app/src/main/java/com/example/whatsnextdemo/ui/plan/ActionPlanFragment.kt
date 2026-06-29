package com.example.whatsnextdemo.ui.plan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.ActionTaskRepository
import com.example.whatsnextdemo.databinding.DialogActionTaskBinding
import com.example.whatsnextdemo.databinding.FragmentActionPlanBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActionPlanFragment : Fragment() {
    private var _binding: FragmentActionPlanBinding? = null
    private val binding: FragmentActionPlanBinding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var actionTaskRepository: ActionTaskRepository
    private lateinit var adapter: ActionTaskAdapter
    private var currentFilter: ActionTaskFilter = ActionTaskFilter.ALL
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val categories: List<String> = listOf(
        ActionTaskValues.CATEGORY_LEARNING,
        ActionTaskValues.CATEGORY_JOB,
        ActionTaskValues.CATEGORY_PROJECT,
        ActionTaskValues.CATEGORY_EXPLORE,
        ActionTaskValues.CATEGORY_OTHER
    )
    private val priorityLabels: List<String> = listOf("高", "中", "低")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        actionTaskRepository = ActionTaskRepository(
            AppDatabase.getInstance(requireContext()).actionTaskDao()
        )
        setupList()
        setupFilterTabs()
        setupActions()
        loadTasks()
    }

    override fun onResume(): Unit {
        super.onResume()
        if (this::actionTaskRepository.isInitialized) {
            loadTasks()
        }
    }

    private fun setupList(): Unit {
        adapter = ActionTaskAdapter(
            onToggleComplete = { task -> toggleTaskStatus(task) },
            onEdit = { task -> showEditTaskDialog(task) },
            onDelete = { task -> confirmDeleteTask(task) }
        )
        binding.rvActionTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActionTasks.adapter = adapter
    }

    private fun setupFilterTabs(): Unit {
        binding.tabTaskFilter.addTab(binding.tabTaskFilter.newTab().setText("全部"))
        binding.tabTaskFilter.addTab(binding.tabTaskFilter.newTab().setText("进行中"))
        binding.tabTaskFilter.addTab(binding.tabTaskFilter.newTab().setText("已完成"))
        binding.tabTaskFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab): Unit {
                currentFilter = when (tab.position) {
                    1 -> ActionTaskFilter.ACTIVE
                    2 -> ActionTaskFilter.COMPLETED
                    else -> ActionTaskFilter.ALL
                }
                loadTasks()
            }

            override fun onTabUnselected(tab: TabLayout.Tab): Unit = Unit

            override fun onTabReselected(tab: TabLayout.Tab): Unit = Unit
        })
    }

    private fun setupActions(): Unit {
        binding.fabAddTask.setOnClickListener {
            showCreateTaskDialog()
        }
        binding.btnEmptyCreateTask.setOnClickListener {
            showCreateTaskDialog()
        }
    }

    private fun loadTasks(): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            renderTasks(emptyList(), 0, 0)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val allTasks: List<ActionTaskEntity>
            val filteredTasks: List<ActionTaskEntity>
            val completedCount: Int
            withContext(Dispatchers.IO) {
                allTasks = actionTaskRepository.getAllTasks(username)
                filteredTasks = when (currentFilter) {
                    ActionTaskFilter.ALL -> allTasks
                    ActionTaskFilter.ACTIVE -> actionTaskRepository.getActiveTasks(username)
                    ActionTaskFilter.COMPLETED -> actionTaskRepository.getCompletedTasks(username)
                }
                completedCount = actionTaskRepository.countCompletedTasks(username)
            }
            renderTasks(filteredTasks, allTasks.size, completedCount)
        }
    }

    private fun renderTasks(tasks: List<ActionTaskEntity>, totalCount: Int, completedCount: Int): Unit {
        val activeCount: Int = totalCount - completedCount
        binding.tvTaskStats.text = "已完成 $completedCount · 进行中 $activeCount · 总任务 $totalCount"
        adapter.submitList(tasks)
        val isEmpty: Boolean = tasks.isEmpty()
        binding.rvActionTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        updateEmptyState()
    }

    private fun updateEmptyState(): Unit {
        when (currentFilter) {
            ActionTaskFilter.ALL -> {
                binding.tvEmptyTitle.text = "暂无行动任务"
                binding.tvEmptyMessage.text = "可以手动新建任务，或从职业报告中导入行动建议。"
            }
            ActionTaskFilter.ACTIVE -> {
                binding.tvEmptyTitle.text = "没有进行中的任务"
                binding.tvEmptyMessage.text = "新建一个任务，继续推进你的职业规划。"
            }
            ActionTaskFilter.COMPLETED -> {
                binding.tvEmptyTitle.text = "还没有已完成任务"
                binding.tvEmptyMessage.text = "完成任务后，这里会保留你的进度记录。"
            }
        }
    }

    private fun showCreateTaskDialog(): Unit {
        val initialState: ActionTaskFormState = ActionTaskFormState(
            title = "",
            note = "",
            category = ActionTaskValues.CATEGORY_LEARNING,
            priority = ActionTaskValues.PRIORITY_MEDIUM,
            dueDate = null
        )
        showTaskFormDialog("新建任务", initialState) { state ->
            createTask(state)
        }
    }

    private fun showEditTaskDialog(task: ActionTaskEntity): Unit {
        val initialState: ActionTaskFormState = ActionTaskFormState(
            title = task.title,
            note = task.note,
            category = task.category,
            priority = task.priority,
            dueDate = task.dueDate
        )
        showTaskFormDialog("编辑任务", initialState) { state ->
            updateTask(task, state)
        }
    }

    private fun showTaskFormDialog(
        dialogTitle: String,
        initialState: ActionTaskFormState,
        onSave: (ActionTaskFormState) -> Unit
    ): Unit {
        val dialogBinding: DialogActionTaskBinding = DialogActionTaskBinding.inflate(layoutInflater)
        val categoryAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spCategory.adapter = categoryAdapter
        dialogBinding.spCategory.setSelection(indexOfCategory(initialState.category))

        val priorityAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorityLabels
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spPriority.adapter = priorityAdapter
        dialogBinding.spPriority.setSelection(indexOfPriority(initialState.priority))

        dialogBinding.etTaskTitle.setText(initialState.title)
        dialogBinding.etTaskNote.setText(initialState.note)
        var selectedDueDate: Long? = initialState.dueDate
        updateDueDateText(dialogBinding, selectedDueDate)
        dialogBinding.tvDueDate.setOnClickListener {
            showDatePicker(selectedDueDate) { pickedDate ->
                selectedDueDate = pickedDate
                updateDueDateText(dialogBinding, selectedDueDate)
            }
        }
        dialogBinding.btnClearDueDate.setOnClickListener {
            selectedDueDate = null
            updateDueDateText(dialogBinding, selectedDueDate)
        }

        val dialog: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title: String = dialogBinding.etTaskTitle.text.toString().trim()
                if (title.isBlank()) {
                    dialogBinding.etTaskTitle.error = "请输入任务标题"
                    return@setOnClickListener
                }
                val state: ActionTaskFormState = ActionTaskFormState(
                    title = title,
                    note = dialogBinding.etTaskNote.text.toString().trim(),
                    category = dialogBinding.spCategory.selectedItem.toString(),
                    priority = priorityValue(dialogBinding.spPriority.selectedItem.toString()),
                    dueDate = selectedDueDate
                )
                onSave(state)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun createTask(state: ActionTaskFormState): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            Toast.makeText(requireContext(), "请先登录后再新建任务", Toast.LENGTH_SHORT).show()
            return
        }
        val task: ActionTaskEntity = ActionTaskEntity(
            id = 0,
            username = username,
            title = state.title,
            note = state.note,
            category = state.category,
            priority = state.priority,
            dueDate = state.dueDate,
            isCompleted = false,
            source = ActionTaskValues.SOURCE_MANUAL,
            sourceReportId = null,
            createTime = System.currentTimeMillis(),
            completeTime = null
        )
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                actionTaskRepository.addTask(task)
            }
            Toast.makeText(requireContext(), "任务已保存", Toast.LENGTH_SHORT).show()
            loadTasks()
        }
    }

    private fun updateTask(task: ActionTaskEntity, state: ActionTaskFormState): Unit {
        val updatedTask: ActionTaskEntity = task.copy(
            title = state.title,
            note = state.note,
            category = state.category,
            priority = state.priority,
            dueDate = state.dueDate
        )
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                actionTaskRepository.updateTask(updatedTask)
            }
            Toast.makeText(requireContext(), "任务已更新", Toast.LENGTH_SHORT).show()
            loadTasks()
        }
    }

    private fun toggleTaskStatus(task: ActionTaskEntity): Unit {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (task.isCompleted) {
                    actionTaskRepository.restoreTask(task.id)
                } else {
                    actionTaskRepository.markTaskCompleted(task.id, System.currentTimeMillis())
                }
            }
            Toast.makeText(
                requireContext(),
                if (task.isCompleted) "任务已恢复为进行中" else "任务已完成",
                Toast.LENGTH_SHORT
            ).show()
            loadTasks()
        }
    }

    private fun confirmDeleteTask(task: ActionTaskEntity): Unit {
        AlertDialog.Builder(requireContext())
            .setTitle("删除任务")
            .setMessage("确定删除“${task.title}”吗？删除后无法恢复。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                deleteTask(task)
            }
            .show()
    }

    private fun deleteTask(task: ActionTaskEntity): Unit {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                actionTaskRepository.deleteTask(task)
            }
            Toast.makeText(requireContext(), "任务已删除", Toast.LENGTH_SHORT).show()
            loadTasks()
        }
    }

    private fun showDatePicker(initialDate: Long?, onDatePicked: (Long) -> Unit): Unit {
        val calendar: Calendar = Calendar.getInstance()
        if (initialDate != null) {
            calendar.timeInMillis = initialDate
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val pickedCalendar: Calendar = Calendar.getInstance()
                pickedCalendar.set(Calendar.YEAR, year)
                pickedCalendar.set(Calendar.MONTH, month)
                pickedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                pickedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                pickedCalendar.set(Calendar.MINUTE, 0)
                pickedCalendar.set(Calendar.SECOND, 0)
                pickedCalendar.set(Calendar.MILLISECOND, 0)
                onDatePicked(pickedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDueDateText(dialogBinding: DialogActionTaskBinding, dueDate: Long?): Unit {
        dialogBinding.tvDueDate.text = dueDate?.let { value ->
            "截止日期：${dateFormatter.format(Date(value))}"
        } ?: "设置截止日期"
    }

    private fun indexOfCategory(category: String): Int {
        val index: Int = categories.indexOf(category)
        return if (index >= 0) index else categories.lastIndex
    }

    private fun indexOfPriority(priority: String): Int {
        return when (priority) {
            ActionTaskValues.PRIORITY_HIGH -> 0
            ActionTaskValues.PRIORITY_LOW -> 2
            else -> 1
        }
    }

    private fun priorityValue(label: String): String {
        return when (label) {
            "高" -> ActionTaskValues.PRIORITY_HIGH
            "低" -> ActionTaskValues.PRIORITY_LOW
            else -> ActionTaskValues.PRIORITY_MEDIUM
        }
    }

    override fun onDestroyView(): Unit {
        binding.rvActionTasks.adapter = null
        super.onDestroyView()
        _binding = null
    }
}

private data class ActionTaskFormState(
    val title: String,
    val note: String,
    val category: String,
    val priority: String,
    val dueDate: Long?
)
