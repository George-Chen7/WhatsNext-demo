package com.example.whatsnextdemo.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.databinding.FragmentReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var reportRepository: CareerReportRepository

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
        reportRepository = CareerReportRepository(
            AppDatabase.getInstance(requireContext()).careerReportDao()
        )
        loadLatestReport()
    }

    private fun loadLatestReport() {
        viewLifecycleOwner.lifecycleScope.launch {
            val username = sessionManager.getUsername().ifBlank { "guest" }
            val latestReport = withContext(Dispatchers.IO) {
                reportRepository.getReports(username).firstOrNull()
            }
            if (latestReport == null) {
                binding.tvReportContent.text = "暂无职业规划报告\n\n完成测评后即可生成 AI 分析报告。"
            } else {
                binding.tvReportContent.text = latestReport.content
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
