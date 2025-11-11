package com.aithink.studycompanion.data.repository

import com.aithink.studycompanion.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class AIRepository {

    // Simulate AI response with fallback content
    suspend fun generateChatResponse(message: String, model: AIModel): Flow<String> = flow {
        // Simulate streaming response
        val response = getAIResponse(message)
        val words = response.split(" ")
        for (word in words) {
            emit("$word ")
            delay(50) // Simulate streaming delay
        }
    }

    suspend fun generateQuiz(topic: String, model: AIModel): Quiz {
        // Generate fallback quiz questions
        val questions = mutableListOf<QuizQuestion>()

        // 7 Easy questions
        repeat(7) {
            questions.add(generateQuestion(topic, Difficulty.EASY))
        }

        // 7 Medium questions
        repeat(7) {
            questions.add(generateQuestion(topic, Difficulty.MEDIUM))
        }

        // 6 Hard questions
        repeat(6) {
            questions.add(generateQuestion(topic, Difficulty.HARD))
        }

        questions.shuffle()

        return Quiz(
            topic = topic,
            questions = questions
        )
    }

    suspend fun generateExplanation(topic: String, model: AIModel): String {
        // Fallback explanation generation
        return buildString {
            appendLine("## Understanding $topic")
            appendLine()
            appendLine("### Introduction")
            appendLine("$topic is an important concept that requires clear understanding. Here's a comprehensive explanation:")
            appendLine()
            appendLine("### Key Concepts")
            appendLine("1. **Fundamental Principles**: The core ideas behind $topic involve...")
            appendLine("2. **Practical Applications**: You can apply $topic in various scenarios...")
            appendLine("3. **Common Examples**: Real-world examples include...")
            appendLine()
            appendLine("### Summary")
            appendLine("To master $topic, focus on understanding the fundamental principles and practice with real examples.")
        }
    }

    suspend fun generatePracticeProblems(topic: String, model: AIModel): List<PracticeProblem> {
        val problems = mutableListOf<PracticeProblem>()

        // Generate 3 MCQs
        repeat(3) {
            problems.add(
                PracticeProblem(
                    problem = "Practice question ${it + 1} about $topic: What is the main concept?",
                    type = ProblemType.MCQ,
                    options = listOf(
                        "Option A: First concept",
                        "Option B: Second concept",
                        "Option C: Third concept",
                        "Option D: Fourth concept"
                    ),
                    correctAnswer = "Option ${('A' + Random.nextInt(4))}: Correct concept"
                )
            )
        }

        // Generate 2 text input problems
        repeat(2) {
            problems.add(
                PracticeProblem(
                    problem = "Explain the ${it + 1}. aspect of $topic in your own words:",
                    type = ProblemType.TEXT_INPUT,
                    correctAnswer = "A detailed explanation of $topic focusing on key concepts and applications."
                )
            )
        }

        return problems
    }

    suspend fun generateKidsContent(contentType: String): String {
        return when (contentType.lowercase()) {
            "alphabets" -> generateAlphabets()
            "numbers" -> generateNumbers()
            "colors" -> generateColors()
            "shapes" -> generateShapes()
            "rhymes" -> generateRhymes()
            else -> "Fun learning content about $contentType!"
        }
    }

    private fun getAIResponse(message: String): String {
        val lowerMessage = message.lowercase()

        return when {
            lowerMessage.contains("hello") || lowerMessage.contains("hi") ->
                "Hello! I'm AiThink, your AI study companion. How can I help you learn today?"

            lowerMessage.contains("what") && lowerMessage.contains("learn") ->
                "I can help you with various subjects! You can ask me questions, generate quizzes, get explanations, or practice problems. What would you like to explore?"

            lowerMessage.contains("quiz") ->
                "I can generate a quiz for you! Just tell me the topic you'd like to be quizzed on."

            lowerMessage.contains("explain") ->
                "I'd be happy to explain that topic! Please provide the specific topic you'd like me to explain."

            lowerMessage.contains("help") ->
                "I'm here to help you learn! You can:\n• Ask me questions about any subject\n• Generate quizzes to test your knowledge\n• Get detailed explanations\n• Practice with problems\n• Track your learning progress"

            else ->
                "That's an interesting question about ${message.take(30)}... Based on standard knowledge, here's what I can tell you: This is a complex topic that involves multiple concepts. Would you like me to break it down into simpler parts or provide specific examples?"
        }
    }

    private fun generateQuestion(topic: String, difficulty: Difficulty): QuizQuestion {
        val questions = when (difficulty) {
            Difficulty.EASY -> getEasyQuestions(topic)
            Difficulty.MEDIUM -> getMediumQuestions(topic)
            Difficulty.HARD -> getHardQuestions(topic)
        }

        return questions.random()
    }

    private fun getEasyQuestions(topic: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What is the basic definition of $topic?",
                options = listOf(
                    "A fundamental concept in the field",
                    "An advanced theoretical framework",
                    "A complex mathematical formula",
                    "A historical event"
                ),
                correctAnswer = 0,
                difficulty = Difficulty.EASY
            ),
            QuizQuestion(
                question = "Which of these is most commonly associated with $topic?",
                options = listOf(
                    "Abstract theories",
                    "Basic principles and concepts",
                    "Advanced research papers",
                    "Experimental data only"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.EASY
            ),
            QuizQuestion(
                question = "What is the primary purpose of studying $topic?",
                options = listOf(
                    "To understand basic concepts",
                    "To write research papers",
                    "To memorize formulas",
                    "To teach others"
                ),
                correctAnswer = 0,
                difficulty = Difficulty.EASY
            ),
            QuizQuestion(
                question = "In simple terms, $topic can be described as:",
                options = listOf(
                    "A core subject area",
                    "An impossible concept",
                    "A temporary trend",
                    "An outdated theory"
                ),
                correctAnswer = 0,
                difficulty = Difficulty.EASY
            )
        )
    }

    private fun getMediumQuestions(topic: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "How does $topic apply to real-world scenarios?",
                options = listOf(
                    "Through practical applications",
                    "Only in theoretical studies",
                    "It has no real applications",
                    "Through complex mathematics only"
                ),
                correctAnswer = 0,
                difficulty = Difficulty.MEDIUM
            ),
            QuizQuestion(
                question = "What is an intermediate concept in $topic?",
                options = listOf(
                    "Basic definitions",
                    "Application of principles to solve problems",
                    "Historical background only",
                    "Advanced research topics"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.MEDIUM
            ),
            QuizQuestion(
                question = "Which approach is most effective when learning $topic?",
                options = listOf(
                    "Memorizing everything",
                    "Understanding concepts and practicing",
                    "Reading once is enough",
                    "Ignoring examples"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.MEDIUM
            ),
            QuizQuestion(
                question = "How do different aspects of $topic relate to each other?",
                options = listOf(
                    "They are completely independent",
                    "Through interconnected concepts",
                    "They don't relate at all",
                    "Only through memorization"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.MEDIUM
            )
        )
    }

    private fun getHardQuestions(topic: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What are the advanced implications of $topic in modern applications?",
                options = listOf(
                    "Simple basic concepts",
                    "No implications exist",
                    "Complex systems integration and optimization",
                    "Historical documentation only"
                ),
                correctAnswer = 2,
                difficulty = Difficulty.HARD
            ),
            QuizQuestion(
                question = "How can you critically evaluate different approaches to $topic?",
                options = listOf(
                    "By accepting everything as true",
                    "By analyzing effectiveness, efficiency, and outcomes",
                    "By ignoring alternative methods",
                    "By following only one approach"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.HARD
            ),
            QuizQuestion(
                question = "What challenges exist when implementing $topic in complex systems?",
                options = listOf(
                    "No challenges exist",
                    "Only simple problems",
                    "Integration complexity, scalability, and optimization",
                    "It cannot be implemented"
                ),
                correctAnswer = 2,
                difficulty = Difficulty.HARD
            ),
            QuizQuestion(
                question = "How does $topic integrate with other advanced concepts?",
                options = listOf(
                    "It doesn't integrate",
                    "Through interdisciplinary connections and shared principles",
                    "Only through memorization",
                    "Through simple definitions"
                ),
                correctAnswer = 1,
                difficulty = Difficulty.HARD
            )
        )
    }

    private fun generateAlphabets(): String {
        return buildString {
            appendLine("## Learning Alphabets A-Z")
            appendLine()
            ('A'..'Z').forEach { letter ->
                appendLine("### $letter - ${letter.lowercase()}")
                appendLine("Example: ${getExampleWord(letter)}")
                appendLine()
            }
        }
    }

    private fun generateNumbers(): String {
        return buildString {
            appendLine("## Learning Numbers 1-10")
            appendLine()
            (1..10).forEach { num ->
                appendLine("### $num")
                appendLine("Count: ${getCountExample(num)}")
                appendLine()
            }
        }
    }

    private fun generateColors(): String {
        val colors = listOf(
            "Red - Like an apple",
            "Blue - Like the sky",
            "Yellow - Like the sun",
            "Green - Like grass",
            "Orange - Like an orange fruit",
            "Purple - Like grapes",
            "Pink - Like a flower",
            "Brown - Like chocolate",
            "Black - Like night",
            "White - Like snow"
        )

        return buildString {
            appendLine("## Learning Colors")
            appendLine()
            colors.forEach { color ->
                appendLine("### $color")
                appendLine()
            }
        }
    }

    private fun generateShapes(): String {
        val shapes = listOf(
            "Circle - Round like a ball",
            "Square - Four equal sides",
            "Triangle - Three sides",
            "Rectangle - Four sides, two long",
            "Oval - Like an egg",
            "Star - Points in the sky",
            "Heart - Symbol of love",
            "Diamond - Like a gem"
        )

        return buildString {
            appendLine("## Learning Shapes")
            appendLine()
            shapes.forEach { shape ->
                appendLine("### $shape")
                appendLine()
            }
        }
    }

    private fun generateRhymes(): String {
        return buildString {
            appendLine("## Fun Nursery Rhymes")
            appendLine()
            appendLine("### 1. Twinkle Twinkle Little Star")
            appendLine("Twinkle, twinkle, little star,")
            appendLine("How I wonder what you are!")
            appendLine()
            appendLine("### 2. Mary Had a Little Lamb")
            appendLine("Mary had a little lamb,")
            appendLine("Its fleece was white as snow.")
            appendLine()
            appendLine("### 3. Baa Baa Black Sheep")
            appendLine("Baa, baa, black sheep,")
            appendLine("Have you any wool?")
            appendLine()
            appendLine("### 4. Humpty Dumpty")
            appendLine("Humpty Dumpty sat on a wall,")
            appendLine("Humpty Dumpty had a great fall.")
            appendLine()
            appendLine("### 5. Jack and Jill")
            appendLine("Jack and Jill went up the hill,")
            appendLine("To fetch a pail of water.")
        }
    }

    private fun getExampleWord(letter: Char): String {
        val examples = mapOf(
            'A' to "Apple", 'B' to "Ball", 'C' to "Cat", 'D' to "Dog",
            'E' to "Elephant", 'F' to "Fish", 'G' to "Goat", 'H' to "House",
            'I' to "Ice cream", 'J' to "Juice", 'K' to "Kite", 'L' to "Lion",
            'M' to "Monkey", 'N' to "Nest", 'O' to "Orange", 'P' to "Pen",
            'Q' to "Queen", 'R' to "Rabbit", 'S' to "Sun", 'T' to "Tree",
            'U' to "Umbrella", 'V' to "Van", 'W' to "Water", 'X' to "Xylophone",
            'Y' to "Yellow", 'Z' to "Zebra"
        )
        return examples[letter] ?: "Word"
    }

    private fun getCountExample(num: Int): String {
        val items =
            listOf("apple", "ball", "star", "flower", "bird", "car", "book", "toy", "tree", "heart")
        return List(num) { items[num - 1] }.joinToString(", ")
    }
}
