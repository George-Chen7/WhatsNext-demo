package com.example.whatsnextdemo.service

import com.example.whatsnextdemo.data.model.UserProfile

interface CareerAiService {
    suspend fun generateCareerReport(profile: UserProfile): String
}
