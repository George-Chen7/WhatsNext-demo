package com.example.whatsnextdemo.ui.ai

import com.example.whatsnextdemo.data.model.AiAnalysisResponse

sealed class AiAnalysisUiState {
    data object Idle : AiAnalysisUiState()
    data object Loading : AiAnalysisUiState()
    data class Success(val response: AiAnalysisResponse) : AiAnalysisUiState()
    data class Error(val message: String) : AiAnalysisUiState()
}
