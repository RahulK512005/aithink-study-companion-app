package com.aithink.studycompanion.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val role: UserRole,
    val purpose: LearningPurpose,
    val memberSince: Long = System.currentTimeMillis(),
    val learningStreak: Int = 0,
    val topicsMastered: Int = 0,
    val questionsAnswered: Int = 0,
    val lastActiveDate: Long = 0L
)

enum class UserRole {
    STUDENT,
    IT_PROFESSIONAL
}

enum class LearningPurpose {
    ACADEMIC_LEARNING,
    SKILL_DEVELOPMENT,
    EXAM_PREPARATION,
    RESEARCH
}

enum class AIModel(val displayName: String) {
    GEMMA_3_1B("Gemma 3 1B"),
    QWEN_2_5_0_5B("Qwen 2.5 0.5B"),
    TINY_LLAMA("TinyLlama")
}

@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class QuizQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val difficulty: Difficulty,
    val userAnswer: Int? = null
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}

@Serializable
data class Quiz(
    val id: String = java.util.UUID.randomUUID().toString(),
    val topic: String,
    val questions: List<QuizQuestion>,
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PracticeProblem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val problem: String,
    val type: ProblemType,
    val options: List<String>? = null,
    val correctAnswer: String,
    val userAnswer: String? = null,
    val isCorrect: Boolean? = null
)

enum class ProblemType {
    MCQ, TEXT_INPUT
}

@Serializable
data class ActivityHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityType {
    SUBJECT_LEARNING,
    QUESTION,
    QUIZ,
    COMPLETED,
    EXPLAIN,
    PRACTICE
}

data class Subject(
    val name: String,
    val icon: String,
    val category: SubjectCategory
)

enum class SubjectCategory {
    LKG_UKG,
    CLASS_1_5,
    CLASS_6_10,
    CLASS_11_12,
    UNDERGRADUATE,
    POSTGRADUATE,
    PHD
}

enum class Domain {
    SCIENCE,
    COMMERCE,
    ARTS,
    ENGINEERING,
    MEDICAL
}

@Serializable
data class LearningStats(
    val learningStreak: Int = 0,
    val topicsMastered: Int = 0,
    val questionsAnswered: Int = 0,
    val accuracyRate: Float = 0f
)
