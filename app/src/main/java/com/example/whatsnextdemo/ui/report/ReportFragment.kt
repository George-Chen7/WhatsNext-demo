package com.example.whatsnextdemo.ui.report

import android.content.Intent
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        binding.btnExportReport.setOnClickListener {
            exportCurrentReport()
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

    private fun exportCurrentReport(): Unit {
        val report: CareerReportEntity = latestReport ?: run {
            Toast.makeText(requireContext(), "当前没有可导出的职业报告", Toast.LENGTH_SHORT).show()
            return
        }
        if (report.content.isBlank()) {
            Toast.makeText(requireContext(), "当前职业报告内容为空，无法导出", Toast.LENGTH_SHORT).show()
            return
        }

        val exportDirectory: File = File(requireContext().filesDir, REPORT_EXPORT_DIRECTORY)
        binding.btnExportReport.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val exportedFile: File = withContext(Dispatchers.IO) {
                    writeReportFile(report, exportDirectory)
                }
                Toast.makeText(
                    requireContext(),
                    "报告已保存：${exportedFile.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (error: IOException) {
                Toast.makeText(
                    requireContext(),
                    "导出失败：${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (error: SecurityException) {
                Toast.makeText(
                    requireContext(),
                    "导出失败：没有写入报告文件的权限，${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                _binding?.btnExportReport?.isEnabled = true
            }
        }
    }

    private fun writeReportFile(report: CareerReportEntity, exportDirectory: File): File {
        if (!exportDirectory.exists() && !exportDirectory.mkdirs()) {
            throw IOException("无法创建目录 ${exportDirectory.absolutePath}")
        }
        if (!exportDirectory.isDirectory) {
            throw IOException("导出路径不是目录 ${exportDirectory.absolutePath}")
        }

        val fileName: String = buildReportFileName(report)
        val reportFile: File = File(exportDirectory, fileName)
        val content: String = buildReportFileContent(report)
        reportFile.writeText(content, Charsets.UTF_8)
        return reportFile
    }

    private fun buildReportFileName(report: CareerReportEntity): String {
        val safeTitle: String = sanitizeFileNamePart(report.title)
        val timestamp: String = formatFileTimestamp(report.createTime)
        return "career_report_${safeTitle}_$timestamp.txt"
    }

    private fun sanitizeFileNamePart(value: String): String {
        val cleanedValue: String = value
            .trim()
            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .trim('_')
        if (cleanedValue.isBlank()) {
            throw IOException("报告标题为空，无法生成导出文件名")
        }
        return cleanedValue
    }

    private fun buildReportFileContent(report: CareerReportEntity): String {
        return """
            报告标题：${report.title}
            生成时间：${formatDisplayTime(report.createTime)}
            MBTI 结果：${report.mbti}
            霍兰德结果：${report.holland}

            报告正文：
            ${report.content}
        """.trimIndent()
    }

    private fun formatFileTimestamp(createTime: Long): String {
        val formatter: SimpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        return formatter.format(Date(createTime))
    }

    private fun formatDisplayTime(createTime: Long): String {
        val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        return formatter.format(Date(createTime))
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

    companion object {
        private const val REPORT_EXPORT_DIRECTORY: String = "career_reports"
    }
}
