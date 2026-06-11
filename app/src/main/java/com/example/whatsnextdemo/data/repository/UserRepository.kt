package com.example.whatsnextdemo.data.repository

import com.example.whatsnextdemo.data.database.dao.UserDao
import com.example.whatsnextdemo.data.database.entity.UserEntity
import com.example.whatsnextdemo.data.model.UserProfileDetails

class UserRepository(private val userDao: UserDao) {
    suspend fun register(username: String, password: String): Result<Long> {
        if (userDao.findByUsername(username) != null) {
            return Result.failure(IllegalArgumentException("用户名已存在"))
        }
        return runCatching {
            userDao.insert(
                UserEntity(
                    username = username,
                    password = password,
                    nickname = username
                )
            )
        }
    }

    suspend fun login(username: String, password: String): UserEntity? {
        return userDao.findByCredentials(username, password)
    }

    suspend fun findUser(username: String): UserEntity? {
        return userDao.findByUsername(username)
    }

    suspend fun updateProfile(username: String, details: UserProfileDetails): Boolean {
        val user = userDao.findByUsername(username) ?: return false
        userDao.update(
            user.copy(
                nickname = details.nickname,
                major = details.major,
                gender = details.gender,
                birthYear = details.birthYear,
                education = details.education,
                schoolType = details.schoolType,
                grade = details.grade,
                graduationPlan = details.graduationPlan,
                expectedIndustries = details.expectedIndustries.joinToString(PROFILE_LIST_SEPARATOR),
                targetPositions = details.targetPositions.joinToString(PROFILE_LIST_SEPARATOR),
                englishLevels = details.englishLevels.joinToString(PROFILE_LIST_SEPARATOR)
            )
        )
        return true
    }

    private companion object {
        private const val PROFILE_LIST_SEPARATOR: String = "、"
    }
}
