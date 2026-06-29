package com.example.whatsnextdemo.ui.assessment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.DialogManualAssessmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManualAssessmentResultDialog(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val sessionManager: SessionManager,
    private val assessmentRepository: AssessmentRepository,
    private val assessmentType: String,
    private val onResultImported: () -> Unit
) {
    fun show(): Unit {
        val dialogBinding: DialogManualAssessmentResultBinding =
            DialogManualAssessmentResultBinding.inflate(layoutInflater)
        val dialog: AlertDialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDialogTitle.text = buildTitle()
        dialogBinding.tvDialogMessage.text = buildMessage()
        dialogBinding.tilManualResult.hint = buildHint()
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnSave.setOnClickListener {
            saveManualResult(dialog, dialogBinding)
        }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        dialog.show()
    }

    private fun saveManualResult(
        dialog: AlertDialog,
        dialogBinding: DialogManualAssessmentResultBinding
    ): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            Toast.makeText(context, "请先登录后再导入测试结果", Toast.LENGTH_SHORT).show()
            return
        }

        val rawResult: String = dialogBinding.etManualResult.text?.toString().orEmpty()
        val result: AssessmentResultEntity = try {
            ManualAssessmentResultFactory.buildResult(
                username = username,
                assessmentType = assessmentType,
                rawResult = rawResult,
                createTime = System.currentTimeMillis()
            )
        } catch (error: IllegalArgumentException) {
            dialogBinding.tilManualResult.error = error.message
            return
        }

        dialogBinding.tilManualResult.error = null
        dialogBinding.btnSave.isEnabled = false
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                assessmentRepository.saveResult(result)
            }
            Toast.makeText(context, "测试结果已导入", Toast.LENGTH_SHORT).show()
            onResultImported()
            dialog.dismiss()
        }
    }

    private fun buildTitle(): String {
        return when (assessmentType) {
            AssessmentScorer.TYPE_HOLLAND -> "导入霍兰德测试结果"
            AssessmentScorer.TYPE_MBTI -> "导入 MBTI 测试结果"
            else -> throw IllegalArgumentException("不支持的测评类型：$assessmentType")
        }
    }

    private fun buildMessage(): String {
        return when (assessmentType) {
            AssessmentScorer.TYPE_HOLLAND -> "请输入霍兰德三字母结果，例如 RIA、SEC、AIS。"
            AssessmentScorer.TYPE_MBTI -> "请输入 MBTI 四字母结果，例如 INTJ、ENFP、ISTP。"
            else -> throw IllegalArgumentException("不支持的测评类型：$assessmentType")
        }
    }

    private fun buildHint(): String {
        return when (assessmentType) {
            AssessmentScorer.TYPE_HOLLAND -> "霍兰德结果"
            AssessmentScorer.TYPE_MBTI -> "MBTI 结果"
            else -> throw IllegalArgumentException("不支持的测评类型：$assessmentType")
        }
    }
}
