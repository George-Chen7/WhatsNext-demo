package com.example.whatsnextdemo.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.MainActivity
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.ReportActionTaskFactory
import com.example.whatsnextdemo.data.model.ReportActionSuggestion
import com.example.whatsnextdemo.data.model.ReportActionSuggestionParser
import com.example.whatsnextdemo.data.repository.ActionTaskRepository
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.databinding.FragmentReportBinding
import com.example.whatsnextdemo.ui.onboarding.AiAnalyzingActivity
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var reportRepository: CareerReportRepository
    private lateinit var actionTaskRepository: ActionTaskRepository
    private var latestReport: CareerReportEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val database: AppDatabase = AppDatabase.getInstance(requireContext())
        reportRepository = CareerReportRepository(database.careerReportDao())
        actionTaskRepository = ActionTaskRepository(database.actionTaskDao())
        binding.btnImportReportActions.setOnClickListener {
            showImportDialog()
        }
        binding.btnGenerateReportFromEmpty.setOnClickListener {
            openAssessmentOrGenerateReport()
        }
        loadLatestReport()
    }

    private fun loadLatestReport() {
        viewLifecycleOwner.lifecycleScope.launch {
            val username = sessionManager.getUsername().ifBlank { "guest" }
            latestReport = withContext(Dispatchers.IO) {
                reportRepository.getReports(username).firstOrNull()
            }
            val report: CareerReportEntity? = latestReport
            if (report == null) {
                binding.tvReportContent.text = "暂无职业规划报告\n\n完成测评后即可生成 AI 分析报告。"
                binding.btnImportReportActions.visibility = View.GONE
                binding.btnGenerateReportFromEmpty.visibility = View.VISIBLE
                binding.btnGenerateReportFromEmpty.text = "去完成测评"
            } else {
                binding.tvReportContent.text = report.content
                binding.btnImportReportActions.visibility = View.VISIBLE
                binding.btnGenerateReportFromEmpty.visibility = View.VISIBLE
                binding.btnGenerateReportFromEmpty.text = "重新生成报告"
            }
        }
    }

    private fun showImportDialog(): Unit {
        val report: CareerReportEntity = latestReport ?: run {
            Toast.makeText(requireContext(), "当前没有可导入的职业报告", Toast.LENGTH_SHORT).show()
            return
        }
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            Toast.makeText(requireContext(), "请先登录后再导入行动建议", Toast.LENGTH_SHORT).show()
            return
        }
        val suggestions: List<ReportActionSuggestion> = ReportActionSuggestionParser.parse(report.content)
        if (suggestions.isEmpty()) {
            Toast.makeText(requireContext(), "当前报告没有可导入内容", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val duplicateIndexes: Set<Int> = withContext(Dispatchers.IO) {
                suggestions.mapIndexedNotNull { index, suggestion ->
                    val imported: Boolean = actionTaskRepository.isReportTaskImported(
                        username,
                        report.id,
                        suggestion.title
                    )
                    if (imported) index else null
                }.toSet()
            }
            showSuggestionSelectionDialog(report, username, suggestions, duplicateIndexes)
        }
    }

    private fun showSuggestionSelectionDialog(
        report: CareerReportEntity,
        username: String,
        suggestions: List<ReportActionSuggestion>,
        duplicateIndexes: Set<Int>
    ): Unit {
        val labels: Array<String> = suggestions.mapIndexed { index, suggestion ->
            val suffix: String = if (duplicateIndexes.contains(index)) "（已导入）" else ""
            "${suggestion.period}：${suggestion.title}$suffix"
        }.toTypedArray()
        val checkedItems: BooleanArray = suggestions.mapIndexed { index, _ ->
            !duplicateIndexes.contains(index)
        }.toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setTitle("选择要导入的行动建议")
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setNegativeButton("取消", null)
            .setPositiveButton("导入") { _, _ ->
                importSelectedSuggestions(report, username, suggestions, checkedItems, duplicateIndexes)
            }
            .show()
    }

    private fun importSelectedSuggestions(
        report: CareerReportEntity,
        username: String,
        suggestions: List<ReportActionSuggestion>,
        checkedItems: BooleanArray,
        duplicateIndexes: Set<Int>
    ): Unit {
        val selectedSuggestions: List<ReportActionSuggestion> = suggestions.filterIndexed { index, _ ->
            checkedItems[index] && !duplicateIndexes.contains(index)
        }
        if (selectedSuggestions.isEmpty()) {
            Toast.makeText(requireContext(), "没有选择新的行动建议", Toast.LENGTH_SHORT).show()
            return
        }
        val tasks: List<ActionTaskEntity> = ReportActionTaskFactory.createTasks(
            username = username,
            report = report,
            suggestions = selectedSuggestions,
            createTime = System.currentTimeMillis()
        )
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                actionTaskRepository.addTasks(tasks)
            }
            Toast.makeText(requireContext(), "已导入 ${tasks.size} 个行动任务", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAssessmentOrGenerateReport(): Unit {
        val report: CareerReportEntity? = latestReport
        if (report == null) {
            val hostActivity = requireActivity()
            if (hostActivity is MainActivity) {
                hostActivity.openAssessmentTab()
            }
            return
        }
        val intent: Intent = Intent(requireContext(), AiAnalyzingActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
