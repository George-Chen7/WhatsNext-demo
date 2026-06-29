package com.example.whatsnextdemo.ui.profile

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
import com.example.whatsnextdemo.R
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.database.entity.UserEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.ActionTaskRepository
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.FragmentProfileBinding
import com.example.whatsnextdemo.databinding.ViewProfileMenuRowBinding
import com.example.whatsnextdemo.ui.login.LoginActivity
import com.example.whatsnextdemo.ui.onboarding.ProfileSetupActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    private lateinit var assessmentRepository: AssessmentRepository
    private lateinit var careerReportRepository: CareerReportRepository
    private lateinit var actionTaskRepository: ActionTaskRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        val database: AppDatabase = AppDatabase.getInstance(requireContext())
        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository(database.userDao())
        assessmentRepository = AssessmentRepository(database.assessmentResultDao())
        careerReportRepository = CareerReportRepository(database.careerReportDao())
        actionTaskRepository = ActionTaskRepository(database.actionTaskDao())

        setupMenuRows()
        setupClickListeners()
        loadProfile()
    }

    override fun onResume(): Unit {
        super.onResume()
        if (this::sessionManager.isInitialized) {
            loadProfile()
        }
    }

    private fun setupMenuRows(): Unit {
        configureMenuRow(binding.rowEditProfile, R.drawable.ic_profile_user, "个人资料", "")
        configureMenuRow(binding.rowHistoryReport, R.drawable.ic_profile_report, "历史报告", "0份")
        configureMenuRow(binding.rowActionPlan, R.drawable.ic_profile_task, "行动计划", "")
        configureMenuRow(binding.rowDataExport, R.drawable.ic_profile_export, "数据导出", "")
        configureMenuRow(binding.rowNotification, R.drawable.ic_profile_bell, "通知与提醒", "")
        configureMenuRow(binding.rowPrivacy, R.drawable.ic_profile_privacy, "隐私与数据", "")
        configureMenuRow(binding.rowAbout, R.drawable.ic_profile_about, "关于应用", "")
    }

    private fun configureMenuRow(
        rowBinding: ViewProfileMenuRowBinding,
        iconResId: Int,
        title: String,
        value: String
    ): Unit {
        rowBinding.ivMenuIcon.setImageResource(iconResId)
        rowBinding.tvMenuTitle.text = title
        rowBinding.tvMenuValue.text = value
    }

    private fun setupClickListeners(): Unit {
        binding.rowEditProfile.root.setOnClickListener {
            val intent: Intent = Intent(requireContext(), ProfileSetupActivity::class.java).apply {
                putExtra(ProfileSetupActivity.EXTRA_RETURN_TO_PROFILE, true)
            }
            startActivity(intent)
        }
        binding.rowHistoryReport.root.setOnClickListener {
            val hostActivity = requireActivity()
            if (hostActivity is MainActivity) {
                hostActivity.openReportTab()
            }
        }
        binding.rowActionPlan.root.setOnClickListener {
            val hostActivity = requireActivity()
            if (hostActivity is MainActivity) {
                hostActivity.openActionPlanTab()
            }
        }
        binding.rowDataExport.root.setOnClickListener {
            Toast.makeText(requireContext(), "数据导出功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.rowNotification.root.setOnClickListener {
            Toast.makeText(requireContext(), "通知与提醒功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.rowPrivacy.root.setOnClickListener {
            showPrivacyDialog()
        }
        binding.rowAbout.root.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("关于应用")
                .setMessage("AI Career Planner 是一个本地优先的职业规划课程设计 Demo，用于完成测评、生成职业报告并管理行动计划。")
                .setPositiveButton("知道了", null)
                .show()
        }
        binding.btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    private fun loadProfile(): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            renderProfile(
                displayName = "未登录用户",
                major = "",
                assessmentCount = 0,
                reportCount = 0,
                activeTaskCount = 0
            )
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val snapshot: ProfileSnapshot = withContext(Dispatchers.IO) {
                val user: UserEntity? = userRepository.findUser(username)
                val assessments: List<AssessmentResultEntity> = assessmentRepository.getResults(username)
                val reports: List<CareerReportEntity> = careerReportRepository.getReports(username)
                val activeTaskCount: Int = actionTaskRepository.getActiveTasks(username).size
                ProfileSnapshot(
                    displayName = displayName(user, username),
                    major = user?.major.orEmpty().trim(),
                    assessmentCount = assessments.size,
                    reportCount = reports.size,
                    activeTaskCount = activeTaskCount
                )
            }
            renderProfile(
                displayName = snapshot.displayName,
                major = snapshot.major,
                assessmentCount = snapshot.assessmentCount,
                reportCount = snapshot.reportCount,
                activeTaskCount = snapshot.activeTaskCount
            )
        }
    }

    private fun renderProfile(
        displayName: String,
        major: String,
        assessmentCount: Int,
        reportCount: Int,
        activeTaskCount: Int
    ): Unit {
        val majorStatus: String = major.ifBlank { "专业信息未完善" }
        val majorLine: String = if (major.isBlank()) "专业：未完善" else "专业：$major"
        binding.tvUsername.text = displayName
        binding.tvAvatar.text = avatarText(displayName)
        binding.tvProfileSubtitle.text = "$majorStatus · 职业规划进行中"
        binding.tvHeaderAssessmentCount.text = assessmentCount.toString()
        binding.tvHeaderReportCount.text = reportCount.toString()
        binding.tvHeaderTaskCount.text = activeTaskCount.toString()
        binding.tvAssessmentCount.text = assessmentCount.toString()
        binding.tvReportCount.text = reportCount.toString()
        binding.tvTaskCount.text = activeTaskCount.toString()
        binding.tvMajor.text = majorLine
        binding.rowHistoryReport.tvMenuValue.text = "${reportCount}份"
    }

    private fun displayName(user: UserEntity?, username: String): String {
        val nickname: String = user?.nickname.orEmpty().trim()
        return nickname.ifBlank { username.ifBlank { "未登录用户" } }
    }

    private fun avatarText(displayName: String): String {
        return displayName.trim().ifBlank { "我" }.take(1).uppercase()
    }

    private fun showPrivacyDialog(): Unit {
        AlertDialog.Builder(requireContext())
            .setTitle("隐私与数据")
            .setMessage(
                "用户资料、测评结果、职业报告和行动计划保存在本机数据库中；登录状态保存在本机配置中。" +
                    "\n\n你可以在应用内修改资料、查看报告、管理行动计划，并在后续版本中导出数据。"
            )
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun confirmLogout(): Unit {
        AlertDialog.Builder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出当前账号吗？")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定退出") { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout(): Unit {
        sessionManager.clearLogin()
        val intent: Intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView(): Unit {
        super.onDestroyView()
        _binding = null
    }
}

private data class ProfileSnapshot(
    val displayName: String,
    val major: String,
    val assessmentCount: Int,
    val reportCount: Int,
    val activeTaskCount: Int
)
