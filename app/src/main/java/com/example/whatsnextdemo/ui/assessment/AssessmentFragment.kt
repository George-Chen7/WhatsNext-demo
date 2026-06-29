package com.example.whatsnextdemo.ui.assessment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.FragmentAssessmentBinding

class AssessmentFragment : Fragment() {
    private var _binding: FragmentAssessmentBinding? = null
    private val binding: FragmentAssessmentBinding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var assessmentRepository: AssessmentRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        assessmentRepository = AssessmentRepository(
            AppDatabase.getInstance(requireContext()).assessmentResultDao()
        )
        binding.btnStartHolland.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_HOLLAND)
        }
        binding.btnStartMbti.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_MBTI)
        }
        binding.tvImportHollandResult.setOnClickListener {
            showManualImportDialog(AssessmentScorer.TYPE_HOLLAND)
        }
        binding.tvImportMbtiResult.setOnClickListener {
            showManualImportDialog(AssessmentScorer.TYPE_MBTI)
        }
    }

    private fun startQuestionActivity(type: String): Unit {
        val intent: Intent = Intent(requireContext(), QuestionActivity::class.java)
            .putExtra(AssessmentScorer.EXTRA_ASSESSMENT_TYPE, type)
        startActivity(intent)
    }

    private fun showManualImportDialog(type: String): Unit {
        ManualAssessmentResultDialog(
            context = requireContext(),
            layoutInflater = layoutInflater,
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            sessionManager = sessionManager,
            assessmentRepository = assessmentRepository,
            assessmentType = type,
            onResultImported = {}
        ).show()
    }

    override fun onDestroyView(): Unit {
        super.onDestroyView()
        _binding = null
    }
}
