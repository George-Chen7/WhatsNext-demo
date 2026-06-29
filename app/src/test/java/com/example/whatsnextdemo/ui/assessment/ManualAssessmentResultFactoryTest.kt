package com.example.whatsnextdemo.ui.assessment

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ManualAssessmentResultFactoryTest {
    @Test
    fun buildResultRejectsBlankInput(): Unit {
        val error: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            ManualAssessmentResultFactory.buildResult(
                username = "alice",
                assessmentType = AssessmentScorer.TYPE_MBTI,
                rawResult = "   ",
                createTime = 1000L
            )
        }

        assertEquals("测试结果不能为空", error.message)
    }

    @Test
    fun buildResultCreatesMbtiEntityWithParsableScores(): Unit {
        val entity = ManualAssessmentResultFactory.buildResult(
            username = "alice",
            assessmentType = AssessmentScorer.TYPE_MBTI,
            rawResult = " intj ",
            createTime = 1000L
        )
        val scores: JSONObject = JSONObject(entity.scoreDetail)

        assertEquals("alice", entity.username)
        assertEquals(AssessmentScorer.TYPE_MBTI, entity.type)
        assertEquals("INTJ", entity.result)
        assertEquals(1, scores.getInt("I"))
        assertEquals(1, scores.getInt("N"))
        assertEquals(1, scores.getInt("T"))
        assertEquals(1, scores.getInt("J"))
        assertEquals(1000L, entity.createTime)
    }

    @Test
    fun buildResultCreatesHollandEntityWithParsableScores(): Unit {
        val entity = ManualAssessmentResultFactory.buildResult(
            username = "alice",
            assessmentType = AssessmentScorer.TYPE_HOLLAND,
            rawResult = " ria ",
            createTime = 1000L
        )
        val scores: JSONObject = JSONObject(entity.scoreDetail)

        assertEquals("alice", entity.username)
        assertEquals(AssessmentScorer.TYPE_HOLLAND, entity.type)
        assertEquals("RIA", entity.result)
        assertEquals(3, scores.getInt("R"))
        assertEquals(2, scores.getInt("I"))
        assertEquals(1, scores.getInt("A"))
        assertEquals(1000L, entity.createTime)
    }
}
