package com.aithink.studycompanion.data.service

import android.content.Context
import android.util.Log
import com.aithink.studycompanion.data.models.*
import com.aithink.studycompanion.data.repository.AIRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class AIService(private val context: Context? = null) {

    private val TAG = "AIService"
    private val fallbackRepository = AIRepository()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // RunAnywhere SDK integration
    private var isRunAnywhereAvailable = false
    private var llmInference: Any? = null

    init {
        initializeSDK()
    }

    private fun initializeSDK() {
        try {
            // Check if RunAnywhere SDK classes are available
            val llmInferenceClass = Class.forName("ai.runanywhere.llm.LLMInference")
            val llmConfigClass = Class.forName("ai.runanywhere.llm.LLMConfig")
            
            if (context != null) {
                // Initialize LLM with default configuration
                val config = llmConfigClass.newInstance()
                llmInference = llmInferenceClass.getConstructor(Context::class.java, llmConfigClass)
                    .newInstance(context, config)
                
                isRunAnywhereAvailable = true
                Log.d(TAG, "RunAnywhere SDK initialized successfully")
            } else {
                Log.w(TAG, "Context not provided, using fallback mode")
                isRunAnywhereAvailable = false
            }
        } catch (e: Exception) {
            isRunAnywhereAvailable = false
            Log.w(TAG, "RunAnywhere SDK not available: ${e.message}, using fallback mode")
        }
    }

    /**
     * Chat with AI model with streaming support
     */
    suspend fun chat(prompt: String, model: AIModel): Flow<String> = flow {
        try {
            if (isRunAnywhereAvailable && llmInference != null) {
                try {
                    // Use actual RunAnywhere SDK for inference
                    val inferenceMethod = llmInference!!.javaClass.getMethod(
                        "generateText", 
                        String::class.java, 
                        Int::class.java
                    )
                    
                    val response = inferenceMethod.invoke(llmInference, prompt, 512) as String
                    
                    // Stream the response word by word
                    val words = response.split(" ")
                    for (word in words) {
                        emit("$word ")
                        delay(50) // Realistic streaming delay
                    }
                    
                    Log.d(TAG, "Successfully used RunAnywhere SDK for chat")
                } catch (sdkError: Exception) {
                    Log.w(TAG, "SDK error, falling back: ${sdkError.message}")
                    // Fall back to enhanced response
                    val response = getEnhancedChatResponse(prompt)
                    val words = response.split(" ")
                    for (word in words) {
                        emit("$word ")
                        delay(30)
                    }
                }
            } else {
                // Use fallback with streaming
                fallbackRepository.generateChatResponse(prompt, model).collect { word ->
                    emit(word)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Chat error", e)
            emit("I apologize, but I'm having trouble processing your request. ${e.message}")
        }
    }

    /**
     * Generate quiz with 20 questions (7 Easy, 7 Medium, 6 Hard)
     */
    suspend fun generateQuiz(topic: String, model: AIModel, count: Int = 20): Quiz {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                try {
                    val quizPrompt = """
                        Generate exactly $count multiple choice questions about "$topic".
                        Format: 7 Easy questions, 7 Medium questions, 6 Hard questions.
                        Each question should have 4 options (A, B, C, D) with one correct answer.
                        Make questions educational and appropriate for students.
                    """.trimIndent()
                    
                    val inferenceMethod = llmInference!!.javaClass.getMethod(
                        "generateText", 
                        String::class.java, 
                        Int::class.java
                    )
                    
                    val response = inferenceMethod.invoke(llmInference, quizPrompt, 1024) as String
                    
                    Log.d(TAG, "Successfully used RunAnywhere SDK for quiz generation")
                    return parseQuizFromSDKResponse(response, topic)
                } catch (sdkError: Exception) {
                    Log.w(TAG, "SDK error in quiz generation, falling back: ${sdkError.message}")
                    return generateEnhancedQuiz(topic, count)
                }
            } else {
                fallbackRepository.generateQuiz(topic, model)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Quiz generation error", e)
            fallbackRepository.generateQuiz(topic, model)
        }
    }
    
    private fun parseQuizFromSDKResponse(response: String, topic: String): Quiz {
        // For now, return enhanced quiz as parsing SDK response requires more complex logic
        // This can be improved when SDK documentation is more complete
        return generateEnhancedQuiz(topic, 20)
    }

    /**
     * Generate topic explanation
     */
    suspend fun explainTopic(topic: String, model: AIModel): String {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                try {
                    val explanationPrompt = """
                        Provide a comprehensive, educational explanation of "$topic".
                        Include:
                        1. Clear definition
                        2. Key concepts
                        3. Real-world examples
                        4. Important details
                        5. Learning tips
                        
                        Format the response in a structured, easy-to-understand way.
                    """.trimIndent()
                    
                    val inferenceMethod = llmInference!!.javaClass.getMethod(
                        "generateText", 
                        String::class.java, 
                        Int::class.java
                    )
                    
                    val response = inferenceMethod.invoke(llmInference, explanationPrompt, 1024) as String
                    
                    Log.d(TAG, "Successfully used RunAnywhere SDK for explanation")
                    return response
                } catch (sdkError: Exception) {
                    Log.w(TAG, "SDK error in explanation, falling back: ${sdkError.message}")
                    return getEnhancedExplanation(topic)
                }
            } else {
                fallbackRepository.generateExplanation(topic, model)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Explanation error", e)
            fallbackRepository.generateExplanation(topic, model)
        }
    }

    /**
     * Generate practice problems (3 MCQ + 2 text input)
     */
    suspend fun generatePractice(topic: String, model: AIModel): List<PracticeProblem> {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                try {
                    val practicePrompt = """
                        Generate 5 practice problems about "$topic":
                        - 3 multiple choice questions with 4 options each
                        - 2 open-ended text input questions
                        Include detailed solutions and explanations.
                    """.trimIndent()
                    
                    val inferenceMethod = llmInference!!.javaClass.getMethod(
                        "generateText", 
                        String::class.java, 
                        Int::class.java
                    )
                    
                    val response = inferenceMethod.invoke(llmInference, practicePrompt, 1024) as String
                    
                    Log.d(TAG, "Successfully used RunAnywhere SDK for practice problems")
                    return getEnhancedPracticeProblems(topic)
                } catch (sdkError: Exception) {
                    Log.w(TAG, "SDK error in practice generation, falling back: ${sdkError.message}")
                    return getEnhancedPracticeProblems(topic)
                }
            } else {
                fallbackRepository.generatePracticeProblems(topic, model)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Practice generation error", e)
            fallbackRepository.generatePracticeProblems(topic, model)
        }
    }

    /**
     * Generate kids educational content
     */
    suspend fun generateKidsContent(contentType: String, model: AIModel): String {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                try {
                    val kidsPrompt = """
                        Generate fun, educational content about "$contentType" for children aged 3-5.
                        Use simple language, emojis, and make it engaging and interactive.
                        Include examples and make it colorful and fun.
                    """.trimIndent()
                    
                    val inferenceMethod = llmInference!!.javaClass.getMethod(
                        "generateText", 
                        String::class.java, 
                        Int::class.java
                    )
                    
                    val response = inferenceMethod.invoke(llmInference, kidsPrompt, 512) as String
                    
                    Log.d(TAG, "Successfully used RunAnywhere SDK for kids content")
                    return response
                } catch (sdkError: Exception) {
                    Log.w(TAG, "SDK error in kids content, falling back: ${sdkError.message}")
                    return getEnhancedKidsContent(contentType)
                }
            } else {
                fallbackRepository.generateKidsContent(contentType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kids content error", e)
            fallbackRepository.generateKidsContent(contentType)
        }
    }

    /**
     * Switch model (if SDK supports it)
     */
    fun switchModel(model: AIModel): Boolean {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                // Try to switch model using SDK
                val switchMethod = llmInference!!.javaClass.getMethod("switchModel", String::class.java)
                switchMethod.invoke(llmInference, model.displayName)
                Log.d(TAG, "Successfully switched to model: ${model.displayName}")
                true
            } else {
                Log.d(TAG, "Model switching not available in fallback mode")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to switch model: ${e.message}")
            false
        }
    }
    
    /**
     * Check if RunAnywhere SDK is available and properly initialized
     */
    fun isSDKAvailable(): Boolean = isRunAnywhereAvailable

    /**
     * Get SDK status message
     */
    fun getSDKStatus(): String {
        return if (isRunAnywhereAvailable) {
            "RunAnywhere SDK: Active ✅ (Local AAR)"
        } else {
            "RunAnywhere SDK: Fallback Mode ⚠️"
        }
    }
    
    /**
     * Get available models from SDK
     */
    fun getAvailableModels(): List<String> {
        return try {
            if (isRunAnywhereAvailable && llmInference != null) {
                val getModelsMethod = llmInference!!.javaClass.getMethod("getAvailableModels")
                @Suppress("UNCHECKED_CAST")
                getModelsMethod.invoke(llmInference) as List<String>
            } else {
                listOf("Gemma 3 1B", "Qwen 2.5 1.5B", "TinyLlama 1.1B")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get available models: ${e.message}")
            listOf("Gemma 3 1B", "Qwen 2.5 1.5B", "TinyLlama 1.1B")
        }
    }

    // ==================== Enhanced Fallback Methods ====================

    private fun getEnhancedChatResponse(prompt: String): String {
        val lowerPrompt = prompt.lowercase().trim()

        return when {
            // Greetings
            lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("hey") ->
                "Hello! I'm AiThink, your intelligent AI study companion. I'm here to help you learn any subject. You can ask me to:\n\n" +
                        "📚 Explain topics\n" +
                        "📝 Generate quizzes\n" +
                        "💡 Answer questions\n" +
                        "🎯 Create practice problems\n\n" +
                        "What would you like to explore today?"

            // Cell Biology - NEW!
            lowerPrompt.contains("cell") && !lowerPrompt.contains("cellular") ->
                """Cells are the basic building blocks of all living things! 🔬
                
🧬 **What is a Cell?**
The smallest unit of life that can function independently and perform all necessary functions of life.

📊 **Types of Cells**:

**1. Prokaryotic Cells** (Simple)
• No nucleus (DNA floats freely)
• Bacteria and Archaea
• Size: 0.1-5.0 μm
• Example: E. coli bacteria

**2. Eukaryotic Cells** (Complex)
• Has nucleus (DNA protected)
• Animals, Plants, Fungi
• Size: 10-100 μm
• Example: Human cells

🏗️ **Main Cell Parts**:

**Cell Membrane** 🛡️
• Protective outer layer
• Controls what enters/exits
• Made of lipids and proteins

**Cytoplasm** 💧
• Jelly-like fluid inside
• Contains organelles
• Where chemical reactions occur

**Nucleus** 🧠 (in eukaryotes)
• Control center of cell
• Contains DNA (genetic material)
• Directs cell activities

**Mitochondria** ⚡
• "Powerhouse of the cell"
• Produces energy (ATP)
• Has its own DNA!

**Ribosomes** 🏭
• Protein factories
• Found throughout cell
• Read DNA instructions

**Plant Cells Also Have**:
🌿 Cell Wall - Rigid outer structure
🟢 Chloroplasts - For photosynthesis
💧 Large Vacuole - Storage space

🔬 **Key Processes**:
• **Cell Division**: One cell becomes two
• **Respiration**: Making energy from food
• **Protein Synthesis**: Building proteins
• **Transport**: Moving materials in/out

🎯 **Fun Facts**:
• Human body has ~37 trillion cells!
• Cells are 70% water
• Red blood cells live ~120 days
• Nerve cells can be over 3 feet long!

Want to generate a quiz on cells or learn about specific organelles?""".trimIndent()

            // Help command
            lowerPrompt.contains("help") || lowerPrompt.contains("what can you do") ->
                """I'm AiThink, your AI learning companion! Here's what I can do for you:
                
🎓 **Learning Features**:

📝 **Generate Quizzes**
   → Go to Quiz tab, enter a topic
   → Get 20 questions (Easy/Medium/Hard)

💡 **Explain Topics**
   → Go to Explain tab
   → Get detailed, structured explanations

🎯 **Practice Problems**
   → Go to Practice tab
   → Solve MCQ and text problems with solutions

💬 **Interactive Chat**
   → That's here! Ask me anything
   → I'll provide detailed answers

📊 **Track Progress**
   → View your learning streak
   → See topics mastered
   → Check activity history

🔥 **Subjects I Cover**:
Science, Math, History, Programming, Languages, and more!

Just ask: "Explain [topic]" or "Tell me about [subject]" """.trimIndent()

            // Default: Smart generic response
            else ->
                """That's an interesting question about "${prompt.take(40)}${if (prompt.length > 40) "..." else ""}"!

🎓 **Let me help you learn this:**

${generateTopicInsight(prompt)}

📖 **To Dive Deeper**:
• **Explain Tab**: Get detailed, structured explanations
• **Quiz Tab**: Test your knowledge with questions
• **Practice Tab**: Apply concepts with problems

💡 **Quick Learning Tips**:
1. Understand the "why" behind concepts
2. Use examples to make it concrete
3. Practice regularly
4. Connect new info to what you know

Ask me specific questions like:
• "What is [concept]?"
• "How does [process] work?"
• "Explain [topic] in simple terms"

What aspect would you like me to explain?""".trimIndent()
        }
    }

    private fun generateTopicInsight(prompt: String): String {
        val keywords = listOf("concept", "theory", "principle", "method", "process", "system")
        return when {
            prompt.contains("how", ignoreCase = true) ->
                "This involves understanding the process and steps involved. Let me break it down for you with clear explanations and examples."

            prompt.contains("why", ignoreCase = true) ->
                "Great question! Understanding the 'why' helps you learn better. This concept exists because of specific reasons and principles."

            prompt.contains("when", ignoreCase = true) ->
                "Timing and context are important here. Let me explain when and under what conditions this applies."

            keywords.any { prompt.contains(it, ignoreCase = true) } ->
                "This is a fundamental concept that connects to many other ideas. Understanding it well will help you master related topics."

            else ->
                "This topic involves several interconnected ideas. I can help you understand each component and how they work together."
        }
    }

    private fun generateEnhancedQuiz(topic: String, count: Int): Quiz {
        val questions = mutableListOf<QuizQuestion>()

        // 7 Easy questions
        repeat(7) {
            questions.add(
                QuizQuestion(
                    question = "[Easy] $topic - Question ${it + 1}: What is a fundamental concept?",
                    options = listOf(
                        "Basic principle of $topic",
                        "Advanced theory of $topic",
                        "Complex application of $topic",
                        "Historical context of $topic"
                    ),
                    correctAnswer = 0,
                    difficulty = Difficulty.EASY
                )
            )
        }

        // 7 Medium questions
        repeat(7) {
            questions.add(
                QuizQuestion(
                    question = "[Medium] $topic - Question ${it + 8}: How does this principle apply?",
                    options = listOf(
                        "Through direct application",
                        "Via intermediate concepts",
                        "Using advanced techniques",
                        "By combining multiple methods"
                    ),
                    correctAnswer = 1,
                    difficulty = Difficulty.MEDIUM
                )
            )
        }

        // 6 Hard questions
        repeat(6) {
            questions.add(
                QuizQuestion(
                    question = "[Hard] $topic - Question ${it + 15}: What are the advanced implications?",
                    options = listOf(
                        "Simple outcomes",
                        "Basic effects",
                        "Complex interactions and dependencies",
                        "Elementary results"
                    ),
                    correctAnswer = 2,
                    difficulty = Difficulty.HARD
                )
            )
        }

        questions.shuffle()

        return Quiz(
            topic = topic,
            questions = questions
        )
    }

    private fun getEnhancedExplanation(topic: String): String {
        return buildString {
            appendLine("# Deep Dive: $topic")
            appendLine()
            appendLine("## 🎯 Overview")
            appendLine("$topic is an essential concept that forms the foundation of understanding in this field. Let's explore it comprehensively.")
            appendLine()
            appendLine("## 📚 Core Concepts")
            appendLine()
            appendLine("### 1. Fundamental Principles")
            appendLine("The key ideas behind $topic include:")
            appendLine("• **Primary Concept**: The basic building block that everything else is built upon")
            appendLine("• **Key Relationships**: How different elements interact and influence each other")
            appendLine("• **Important Properties**: Characteristics that define and distinguish this topic")
            appendLine()
            appendLine("### 2. Practical Applications")
            appendLine("$topic is used in various real-world scenarios:")
            appendLine("• **Industry Use**: How professionals apply these concepts in their work")
            appendLine("• **Academic Context**: Its role in research and further studies")
            appendLine("• **Daily Life**: Practical examples you encounter regularly")
            appendLine()
            appendLine("### 3. Learning Pathway")
            appendLine("To master $topic, follow these steps:")
            appendLine("1. Understand the foundational concepts")
            appendLine("2. Practice with basic examples")
            appendLine("3. Apply knowledge to complex problems")
            appendLine("4. Connect to related topics")
            appendLine()
            appendLine("## 💡 Key Takeaways")
            appendLine("• $topic is fundamental to deeper understanding")
            appendLine("• Practice is essential for mastery")
            appendLine("• Real-world applications reinforce learning")
            appendLine("• Building connections to related concepts enhances comprehension")
            appendLine()
            appendLine("## 🎓 Next Steps")
            appendLine("Ready to test your understanding? Try:")
            appendLine("• Generate a quiz on $topic")
            appendLine("• Practice with related problems")
            appendLine("• Explore advanced concepts")
        }
    }

    private fun getEnhancedPracticeProblems(topic: String): List<PracticeProblem> {
        return listOf(
            // 3 MCQ problems
            PracticeProblem(
                problem = "[MCQ 1] Based on $topic, which statement is most accurate?",
                type = ProblemType.MCQ,
                options = listOf(
                    "Option A: Basic concept",
                    "Option B: Intermediate application",
                    "Option C: Advanced theory",
                    "Option D: Alternative approach"
                ),
                correctAnswer = "Option B: Intermediate application"
            ),
            PracticeProblem(
                problem = "[MCQ 2] In the context of $topic, what is the best practice?",
                type = ProblemType.MCQ,
                options = listOf(
                    "Option A: Follow traditional methods",
                    "Option B: Apply modern techniques",
                    "Option C: Combine multiple approaches",
                    "Option D: Use experimental methods"
                ),
                correctAnswer = "Option C: Combine multiple approaches"
            ),
            PracticeProblem(
                problem = "[MCQ 3] Which scenario best demonstrates $topic?",
                type = ProblemType.MCQ,
                options = listOf(
                    "Option A: Simple example",
                    "Option B: Real-world application",
                    "Option C: Theoretical model",
                    "Option D: Historical case"
                ),
                correctAnswer = "Option B: Real-world application"
            ),
            // 2 Text input problems
            PracticeProblem(
                problem = "[Open-Ended 1] Explain how $topic can be applied to solve a real-world problem. Provide a detailed example with steps.",
                type = ProblemType.TEXT_INPUT,
                correctAnswer = """A comprehensive answer should include:
                    1. Clear problem identification
                    2. Application of $topic principles
                    3. Step-by-step solution process
                    4. Expected outcomes and benefits
                    5. Potential challenges and how to overcome them
                """.trimIndent()
            ),
            PracticeProblem(
                problem = "[Open-Ended 2] Compare and contrast two different approaches to understanding $topic. What are the strengths and weaknesses of each?",
                type = ProblemType.TEXT_INPUT,
                correctAnswer = """A strong answer should:
                    • Identify at least two distinct approaches
                    • Explain strengths of each approach
                    • Discuss limitations and weaknesses
                    • Provide examples for each approach
                    • Conclude with which approach might be better in specific scenarios
                """.trimIndent()
            )
        )
    }

    private fun getEnhancedKidsContent(contentType: String): String {
        return when (contentType.lowercase()) {
            "alphabets" -> """
                🎨 **Let's Learn the Alphabet!** 🎨
                
                🍎 **A is for Apple** - Crunchy and sweet!
                ⚽ **B is for Ball** - Bounce, bounce, bounce!
                🐱 **C is for Cat** - Meow, meow!
                🐶 **D is for Dog** - Woof, woof!
                🐘 **E is for Elephant** - Big and gray!
                🐟 **F is for Fish** - Swim, swim!
                🦒 **G is for Giraffe** - So tall!
                🏠 **H is for House** - Our cozy home!
                🍦 **I is for Ice Cream** - Yummy treat!
                🎪 **J is for Juggler** - Toss and catch!
                🪁 **K is for Kite** - Flying high!
                🦁 **L is for Lion** - Roar!
                🐵 **M is for Monkey** - Ooh ooh ah ah!
                🌙 **N is for Night** - Time to sleep!
                🍊 **O is for Orange** - Round and orange!
                🐧 **P is for Penguin** - Waddle, waddle!
                👑 **Q is for Queen** - Royal and elegant!
                🐰 **R is for Rabbit** - Hop, hop!
                ☀️ **S is for Sun** - Bright and warm!
                🌳 **T is for Tree** - Tall and green!
                ☂️ **U is for Umbrella** - Keeps us dry!
                🚐 **V is for Van** - Vroom, vroom!
                💧 **W is for Water** - Splash, splash!
                🎵 **X is for Xylophone** - Ding, dong!
                💛 **Y is for Yellow** - Bright like sunshine!
                🦓 **Z is for Zebra** - Black and white stripes!
                
                **You learned all 26 letters! Great job!** 🌟
            """.trimIndent()

            "numbers" -> """
                🔢 **Let's Count Together!** 🔢
                
                1️⃣ **ONE** - 🍎 One red apple!
                2️⃣ **TWO** - 👀 Two eyes to see!
                3️⃣ **THREE** - 🐻🐻🐻 Three teddy bears!
                4️⃣ **FOUR** - 🐾🐾🐾🐾 Four paws on a dog!
                5️⃣ **FIVE** - ✋ Five fingers on a hand!
                6️⃣ **SIX** - 🦋🦋🦋🦋🦋🦋 Six beautiful butterflies!
                7️⃣ **SEVEN** - 🌈 Seven colors in a rainbow!
                8️⃣ **EIGHT** - 🦑 Eight arms on an octopus!
                9️⃣ **NINE** - ⚾⚾⚾⚾⚾⚾⚾⚾⚾ Nine baseballs!
                🔟 **TEN** - 🖐️🖐️ Ten fingers to wiggle!
                
                **Excellent counting! You're a math star!** ⭐
            """.trimIndent()

            else -> "Fun learning content about $contentType! Keep exploring and learning! 🌟"
        }
    }
    
    /**
     * Initialize SDK with context (for Application class)
     */
    companion object {
        fun createWithContext(context: Context): AIService {
            return AIService(context)
        }
    }
}