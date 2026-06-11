package com.example.whatsnextdemo.ui.assessment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.whatsnextdemo.databinding.FragmentAssessmentBinding

class AssessmentFragment : Fragment() {
    private var _binding: FragmentAssessmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStartHolland.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_HOLLAND)
        }
        binding.btnStartMbti.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_MBTI)
        }
        binding.btnStartCareerAbility.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_CAREER_ABILITY)
        }
        binding.btnStartCareerAnchor.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_CAREER_ANCHOR)
        }
        binding.btnStartCareerValues.setOnClickListener {
            startQuestionActivity(AssessmentScorer.TYPE_CAREER_VALUES)
        }
    }

    private fun startQuestionActivity(type: String) {
        val intent = Intent(requireContext(), QuestionActivity::class.java)
            .putExtra(AssessmentScorer.EXTRA_ASSESSMENT_TYPE, type)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
