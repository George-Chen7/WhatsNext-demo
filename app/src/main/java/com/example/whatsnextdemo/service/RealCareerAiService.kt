package com.example.whatsnextdemo.service

import com.example.whatsnextdemo.data.model.UserProfile

class RealCareerAiService : CareerAiService {
    override suspend fun generateCareerReport(profile: UserProfile): String {
        return "真实 AI API 暂未启用，当前课程演示默认使用 MockCareerAiService。"
    }
}
