package com.example.whatsnextdemo.data.repository

import com.example.whatsnextdemo.data.database.dao.CareerReportDao
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity

class CareerReportRepository(private val careerReportDao: CareerReportDao) {
    suspend fun saveReport(report: CareerReportEntity): Long {
        return careerReportDao.insert(report)
    }

    suspend fun getReports(username: String): List<CareerReportEntity> {
        return careerReportDao.findByUsername(username)
    }

    suspend fun searchReports(username: String, keyword: String): List<CareerReportEntity> {
        return careerReportDao.search(username, keyword)
    }

    suspend fun deleteReport(report: CareerReportEntity) {
        careerReportDao.delete(report)
    }
}
