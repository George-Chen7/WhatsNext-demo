package com.example.whatsnextdemo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.UserProfileDetails
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.ActivityProfileSetupBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        sessionManager = SessionManager(this)
        userRepository = UserRepository(AppDatabase.getInstance(this).userDao())
        configureScreenMode()
        preloadUser()

        binding.btnNextAssessment.setOnClickListener {
            saveProfile()
        }
    }

    private fun configureScreenMode() {
        if (!isEditMode()) {
            return
        }
        binding.tvProfileSetupTitle.text = "修改个人信息"
        binding.tvProfileSetupSubtitle.text = "更新后的资料会同步用于我的页面和 AI 职业分析报告"
        binding.btnNextAssessment.text = "保存修改"
    }

    private fun preloadUser() {
        lifecycleScope.launch {
            val username = sessionManager.getUsername()
            val user = withContext(Dispatchers.IO) {
                userRepository.findUser(username)
            }
            binding.etNickname.setText(user?.nickname.orEmpty().ifBlank { username })
            binding.etMajor.setText(user?.major.orEmpty())
            binding.etBirthYear.setText(user?.birthYear?.toString().orEmpty())
            setRadioSelection(binding.rgGender, user?.gender)
            setRadioSelection(binding.rgEducation, user?.education)
            setRadioSelection(binding.rgSchoolType, user?.schoolType)
            setRadioSelection(binding.rgGrade, user?.grade)
            setRadioSelection(binding.rgGraduationPlan, user?.graduationPlan)
            setCheckedValues(industryCheckBoxes(), user?.expectedIndustries)
            setCheckedValues(targetPositionCheckBoxes(), user?.targetPositions)
            setCheckedValues(englishLevelCheckBoxes(), user?.englishLevels)
        }
    }

    private fun saveProfile() {
        val username: String = sessionManager.getUsername()
        val details: UserProfileDetails = try {
            buildProfileDetails()
        } catch (error: IllegalArgumentException) {
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnNextAssessment.isEnabled = false
        lifecycleScope.launch {
            val updated = withContext(Dispatchers.IO) {
                userRepository.updateProfile(username, details)
            }
            binding.btnNextAssessment.isEnabled = true
            if (!updated) {
                Toast.makeText(this@ProfileSetupActivity, "用户信息不存在，请重新登录", Toast.LENGTH_SHORT).show()
                return@launch
            }

            sessionManager.setProfileCompleted()
            if (isEditMode()) {
                Toast.makeText(this@ProfileSetupActivity, "个人信息已保存", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            startActivity(Intent(this@ProfileSetupActivity, AssessmentGuideActivity::class.java))
        }
    }

    private fun buildProfileDetails(): UserProfileDetails {
        val birthYearText: String = requireText(binding.etBirthYear.text?.toString(), "请填写出生年份")
        val birthYear: Int = birthYearText.toIntOrNull()
            ?: throw IllegalArgumentException("出生年份必须是 4 位数字")
        val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        if (birthYear !in MIN_BIRTH_YEAR..currentYear) {
            throw IllegalArgumentException("出生年份必须在 $MIN_BIRTH_YEAR 到 $currentYear 之间")
        }

        return UserProfileDetails(
            nickname = requireText(binding.etNickname.text?.toString(), "请填写姓名或昵称"),
            gender = requireSelectedRadioText(binding.rgGender, "请选择性别"),
            birthYear = birthYear,
            education = requireSelectedRadioText(binding.rgEducation, "请选择当前学历"),
            schoolType = requireSelectedRadioText(binding.rgSchoolType, "请选择学校类型"),
            major = requireText(binding.etMajor.text?.toString(), "请填写专业"),
            grade = requireSelectedRadioText(binding.rgGrade, "请选择年级"),
            graduationPlan = requireSelectedRadioText(binding.rgGraduationPlan, "请选择毕业后的计划"),
            expectedIndustries = requireCheckedValues(industryCheckBoxes(), "请至少选择一个希望从事的行业"),
            targetPositions = requireCheckedValues(targetPositionCheckBoxes(), "请至少选择一个理想岗位或技能方向"),
            englishLevels = requireCheckedValues(englishLevelCheckBoxes(), "请至少选择一个英语水平")
        )
    }

    private fun isEditMode(): Boolean {
        return intent.getBooleanExtra(EXTRA_RETURN_AFTER_SAVE, false)
    }

    private fun requireText(value: String?, errorMessage: String): String {
        val trimmed: String = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException(errorMessage)
        }
        return trimmed
    }

    private fun requireSelectedRadioText(group: RadioGroup, errorMessage: String): String {
        val checkedId: Int = group.checkedRadioButtonId
        if (checkedId == RadioGroup.NO_ID) {
            throw IllegalArgumentException(errorMessage)
        }
        val selected: RadioButton = findViewById(checkedId)
        return selected.text.toString()
    }

    private fun requireCheckedValues(checkBoxes: List<CheckBox>, errorMessage: String): List<String> {
        val values: List<String> = checkBoxes
            .filter { checkBox -> checkBox.isChecked }
            .map { checkBox -> checkBox.text.toString() }
        if (values.isEmpty()) {
            throw IllegalArgumentException(errorMessage)
        }
        return values
    }

    private fun setRadioSelection(group: RadioGroup, value: String?) {
        if (value.isNullOrBlank()) {
            return
        }
        for (index: Int in 0 until group.childCount) {
            val child: View = group.getChildAt(index)
            if (child is RadioButton && child.text.toString() == value) {
                child.isChecked = true
                return
            }
        }
    }

    private fun setCheckedValues(checkBoxes: List<CheckBox>, joinedValues: String?) {
        if (joinedValues.isNullOrBlank()) {
            return
        }
        val values: Set<String> = joinedValues.split(PROFILE_LIST_SEPARATOR)
            .map { value -> value.trim() }
            .filter { value -> value.isNotEmpty() }
            .toSet()
        checkBoxes.forEach { checkBox ->
            checkBox.isChecked = values.contains(checkBox.text.toString())
        }
    }

    private fun industryCheckBoxes(): List<CheckBox> {
        return listOf(
            binding.cbIndustryIt,
            binding.cbIndustryFinance,
            binding.cbIndustryEducation,
            binding.cbIndustryMedical,
            binding.cbIndustryLaw,
            binding.cbIndustryManufacturing,
            binding.cbIndustryMedia,
            binding.cbIndustryGovernment
        )
    }

    private fun targetPositionCheckBoxes(): List<CheckBox> {
        return listOf(
            binding.cbPositionJava,
            binding.cbPositionKotlin,
            binding.cbPositionPython,
            binding.cbPositionCpp,
            binding.cbPositionOffice,
            binding.cbPositionPhotoshop,
            binding.cbPositionVideo,
            binding.cbPositionData
        )
    }

    private fun englishLevelCheckBoxes(): List<CheckBox> {
        return listOf(
            binding.cbEnglishCet4,
            binding.cbEnglishCet6,
            binding.cbEnglishIelts,
            binding.cbEnglishToefl,
            binding.cbEnglishOther
        )
    }

    companion object {
        const val EXTRA_RETURN_AFTER_SAVE: String = "extra_return_after_save"
        private const val MIN_BIRTH_YEAR: Int = 1970
        private const val PROFILE_LIST_SEPARATOR: String = "、"
    }
}
