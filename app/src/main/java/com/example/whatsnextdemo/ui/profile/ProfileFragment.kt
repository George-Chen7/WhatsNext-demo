package com.example.whatsnextdemo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.FragmentProfileBinding
import com.example.whatsnextdemo.receiver.ForceOfflineReceiver
import com.example.whatsnextdemo.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    private lateinit var careerReportRepository: CareerReportRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = AppDatabase.getInstance(requireContext())
        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository(database.userDao())
        careerReportRepository = CareerReportRepository(database.careerReportDao())

        setupClickListeners()
        loadProfile()
    }

    private fun setupClickListeners() {
        binding.rowEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "资料编辑将在后续阶段完善", Toast.LENGTH_SHORT).show()
        }
        binding.rowHistoryReport.setOnClickListener {
            Toast.makeText(requireContext(), "请在底部“报告”页面查看历史报告", Toast.LENGTH_SHORT).show()
        }
        binding.rowAbout.setOnClickListener {
            Toast.makeText(requireContext(), "AI Career Planner 课程设计 Demo", Toast.LENGTH_SHORT).show()
        }
        binding.btnForceOffline.setOnClickListener {
            val intent = Intent(requireContext(), ForceOfflineReceiver::class.java).apply {
                action = Constants.ACTION_FORCE_OFFLINE
            }
            requireContext().sendBroadcast(intent)
        }
    }

    private fun loadProfile() {
        val username = sessionManager.getUsername()
        binding.tvUsername.text = username.ifBlank { "未登录用户" }
        binding.tvAvatar.text = username.ifBlank { "我" }.take(1).uppercase()
        binding.tvMajor.text = "专业信息未填写"

        if (username.isBlank()) {
            binding.tvReportCount.text = "0"
            binding.tvProfileSubtitle.text = "请登录后查看个人资料"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                userRepository.findUser(username)
            }
            val reports = withContext(Dispatchers.IO) {
                careerReportRepository.getReports(username)
            }
            binding.tvUsername.text = user?.nickname?.ifBlank { username } ?: username
            binding.tvMajor.text = user?.major?.ifBlank { null } ?: "专业信息未填写"
            binding.tvReportCount.text = reports.size.toString()
            binding.tvProfileSubtitle.text = "本地账号 · Room + SharedPreferences"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
