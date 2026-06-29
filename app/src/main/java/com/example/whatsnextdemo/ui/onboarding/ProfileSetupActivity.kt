package com.example.whatsnextdemo.ui.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.R
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.UserEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.UserProfileForm
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.ActivityProfileSetupBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    private var shouldReturnToProfile: Boolean = false
    private var isSaving: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        shouldReturnToProfile = intent.getBooleanExtra(EXTRA_RETURN_TO_PROFILE, false)
        sessionManager = SessionManager(this)
        userRepository = UserRepository(AppDatabase.getInstance(this).userDao())

        setupGradeField()
        setupKeyboardFlow()
        setupTextWatchers()
        setupClickListeners()
        preloadUser()

        if (shouldReturnToProfile) {
            binding.btnNextAssessment.text = getString(R.string.profile_setup_save_only)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { focusedView: View ->
                if (isTouchOutsideView(event, focusedView)) {
                    hideKeyboard(focusedView)
                    focusedView.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun setupGradeField(): Unit {
        val grades: Array<String> = resources.getStringArray(R.array.profile_setup_grade_options)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, grades)
        binding.actvGrade.setAdapter(adapter)
        binding.actvGrade.keyListener = null
        binding.actvGrade.setOnItemClickListener { _, _, _, _ ->
            binding.tilGrade.error = null
            binding.etTargetCareer.requestFocus()
        }
    }

    private fun setupKeyboardFlow(): Unit {
        binding.etNickname.setOnEditorActionListener { _, actionId: Int, _ ->
            handleNextAction(actionId, binding.etMajor)
        }
        binding.etMajor.setOnEditorActionListener { _, actionId: Int, _ ->
            handleNextAction(actionId, binding.actvGrade)
        }
        binding.etTargetCareer.setOnEditorActionListener { _, actionId: Int, _ ->
            handleNextAction(actionId, binding.etInterestedIndustry)
        }
        binding.etInterestedIndustry.setOnEditorActionListener { view, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(view)
                view.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun setupTextWatchers(): Unit {
        binding.etNickname.doAfterTextChanged { binding.tilNickname.error = null }
        binding.etMajor.doAfterTextChanged { binding.tilMajor.error = null }
        binding.etTargetCareer.doAfterTextChanged { binding.tilTargetCareer.error = null }
    }

    private fun setupClickListeners(): Unit {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnNextAssessment.setOnClickListener {
            saveProfile()
        }
    }

    private fun preloadUser(): Unit {
        lifecycleScope.launch {
            val username: String = sessionManager.getUsername()
            val user: UserEntity? = withContext(Dispatchers.IO) {
                userRepository.findUser(username)
            }

            if (username.isNotBlank()) {
                renderProfile(user)
            } else {
                Toast.makeText(
                    this@ProfileSetupActivity,
                    R.string.profile_setup_load_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderProfile(user: UserEntity?): Unit {
        binding.etNickname.setText(user?.nickname.orEmpty())
        binding.etMajor.setText(user?.major.orEmpty())
        binding.actvGrade.setText(user?.grade.orEmpty(), false)
        binding.etTargetCareer.setText(user?.targetCareer.orEmpty())
        binding.etInterestedIndustry.setText(user?.interestedIndustry.orEmpty())
        binding.etStrengths.setText(user?.strengths.orEmpty())
    }

    private fun saveProfile(): Unit {
        if (isSaving) {
            return
        }

        val profile: UserProfileForm = readForm()
        if (!validateForm(profile)) {
            return
        }

        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            Toast.makeText(this, R.string.profile_setup_missing_user, Toast.LENGTH_SHORT).show()
            return
        }

        setSavingState(true)
        lifecycleScope.launch {
            val updated: Boolean = withContext(Dispatchers.IO) {
                userRepository.updateProfile(username, profile)
            }
            setSavingState(false)

            if (!updated) {
                Toast.makeText(
                    this@ProfileSetupActivity,
                    R.string.profile_setup_missing_user,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            sessionManager.setProfileCompleted()
            if (shouldReturnToProfile) {
                Toast.makeText(
                    this@ProfileSetupActivity,
                    R.string.profile_setup_saved,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                val intent: Intent = Intent(this@ProfileSetupActivity, AssessmentGuideActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun readForm(): UserProfileForm {
        return UserProfileForm(
            nickname = binding.etNickname.text?.toString()?.trim().orEmpty(),
            major = binding.etMajor.text?.toString()?.trim().orEmpty(),
            grade = binding.actvGrade.text?.toString()?.trim().orEmpty(),
            targetCareer = binding.etTargetCareer.text?.toString()?.trim().orEmpty(),
            interestedIndustry = binding.etInterestedIndustry.text?.toString()?.trim().orEmpty(),
            strengths = binding.etStrengths.text?.toString()?.trim().orEmpty()
        )
    }

    private fun validateForm(profile: UserProfileForm): Boolean {
        clearErrors()

        val firstInvalidField: TextInputLayout? = when {
            profile.nickname.isBlank() -> binding.tilNickname.apply {
                error = getString(R.string.profile_setup_error_nickname_required)
            }
            !hasValidLength(profile.nickname, MIN_NICKNAME_LENGTH, MAX_NICKNAME_LENGTH) -> binding.tilNickname.apply {
                error = getString(R.string.profile_setup_error_nickname_length)
            }
            profile.major.isBlank() -> binding.tilMajor.apply {
                error = getString(R.string.profile_setup_error_major_required)
            }
            !hasValidLength(profile.major, MIN_MAJOR_LENGTH, MAX_MAJOR_LENGTH) -> binding.tilMajor.apply {
                error = getString(R.string.profile_setup_error_major_length)
            }
            profile.grade.isBlank() -> binding.tilGrade.apply {
                error = getString(R.string.profile_setup_error_grade_required)
            }
            profile.targetCareer.isBlank() -> binding.tilTargetCareer.apply {
                error = getString(R.string.profile_setup_error_target_career_required)
            }
            !hasValidLength(profile.targetCareer, MIN_TARGET_CAREER_LENGTH, MAX_TARGET_CAREER_LENGTH) ->
                binding.tilTargetCareer.apply {
                    error = getString(R.string.profile_setup_error_target_career_length)
                }
            else -> null
        }

        firstInvalidField?.let { field: TextInputLayout ->
            field.requestFocus()
            binding.profileSetupRoot.post {
                binding.profileSetupRoot.smoothScrollTo(0, field.top)
            }
        }
        return firstInvalidField == null
    }

    private fun clearErrors(): Unit {
        binding.tilNickname.error = null
        binding.tilMajor.error = null
        binding.tilGrade.error = null
        binding.tilTargetCareer.error = null
        binding.tilInterestedIndustry.error = null
        binding.tilStrengths.error = null
    }

    private fun hasValidLength(value: String, minLength: Int, maxLength: Int): Boolean {
        val length: Int = value.length
        return length in minLength..maxLength
    }

    private fun setSavingState(saving: Boolean): Unit {
        isSaving = saving
        binding.btnNextAssessment.isEnabled = !saving
    }

    private fun handleNextAction(actionId: Int, nextView: View): Boolean {
        return if (actionId == EditorInfo.IME_ACTION_NEXT) {
            nextView.requestFocus()
            if (nextView === binding.actvGrade) {
                binding.actvGrade.showDropDown()
            }
            true
        } else {
            false
        }
    }

    private fun hideKeyboard(view: View): Unit {
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isTouchOutsideView(event: MotionEvent, view: View): Boolean {
        val bounds: Rect = Rect()
        view.getGlobalVisibleRect(bounds)
        val rawX: Int = event.rawX.toInt()
        val rawY: Int = event.rawY.toInt()
        return !bounds.contains(rawX, rawY)
    }

    companion object {
        const val EXTRA_RETURN_TO_PROFILE: String = "return_to_profile"
        private const val MIN_NICKNAME_LENGTH: Int = 2
        private const val MAX_NICKNAME_LENGTH: Int = 12
        private const val MIN_MAJOR_LENGTH: Int = 2
        private const val MAX_MAJOR_LENGTH: Int = 30
        private const val MIN_TARGET_CAREER_LENGTH: Int = 2
        private const val MAX_TARGET_CAREER_LENGTH: Int = 30
    }
}
