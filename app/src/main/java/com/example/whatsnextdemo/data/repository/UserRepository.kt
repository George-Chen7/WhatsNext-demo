package com.example.whatsnextdemo.data.repository

import com.example.whatsnextdemo.data.database.dao.UserDao
import com.example.whatsnextdemo.data.database.entity.UserEntity

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
}
