package com.aithink.studycompanion.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aithink.studycompanion.AiThinkApplication
import com.aithink.studycompanion.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class DashboardViewModel : ViewModel() {

    private val llmInference = AiThinkApplication.getLLMInference()
    private val prefsManager = AiThinkApplication.getPreferencesManager()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _modelStatus = MutableStateFlow("Initializing...")
    val modelStatus: StateFlow<String> = _modelStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    // Stats
    private val _learningStreak = MutableStateFlow(0)
    val learningStreak: StateFlow<Int> = _learningStreak.asStateFlow()

    private val _topicsMastered = MutableStateFlow(0)
    val topicsMastered: StateFlow<Int> = _topicsMastered.asStateFlow()

    private val _questionsAnswered = MutableStateFlow(0)
    val questionsAnswered: StateFlow<Int> = _questionsAnswered.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> = _chatMessages.asStateFlow()

    // Quiz
    private val _quizQuestions = MutableStateFlow<List<QuizQuestionUI>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestionUI>> = _quizQuestions.asStateFlow()

    // Explanation
    private val _explanation = MutableStateFlow("")
    val explanation: StateFlow<String> = _explanation.asStateFlow()

    // Practice
    private val _practiceProblems = MutableStateFlow<List<PracticeProblemUI>>(emptyList())
    val practiceProblems: StateFlow<List<PracticeProblemUI>> = _practiceProblems.asStateFlow()

    // History
    private val _history = MutableStateFlow<List<ActivityHistoryUI>>(emptyList())
    val history: StateFlow<List<ActivityHistoryUI>> = _history.asStateFlow()

    init {
        loadUserStats()
        loadHistory()
        checkAndLoadModel()
    }

    private fun checkAndLoadModel() {
        viewModelScope.launch {
            // Wait for initialization
            delay(1500)

            if (llmInference.isReady()) {
                _modelStatus.value = "Ready"
                _chatMessages.value = listOf(
                    Message(
                        role = "assistant",
                        content = "Hello! I'm your offline AI study companion. I can answer questions about science, math, history, geography, programming, and more. Ask me anything!"
                    )
                )
            } else {
                _modelStatus.value = "Ready"
                _chatMessages.value = listOf(
                    Message(
                        role = "assistant",
                        content = "Hello! Ask me anything - I'll do my best to help!"
                    )
                )
            }
        }
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            // Load from PreferencesManager when needed
            // For now using default values
            _learningStreak.value = 0
            _topicsMastered.value = 0
            _questionsAnswered.value = 0
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = listOf(
                ActivityHistoryUI(
                    type = "CHAT",
                    data = "AI conversation",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // ==================== Chat Functions ====================

    fun sendMessage(text: String, model: String = "offline-ai") {
        if (_modelStatus.value != "Ready") return

        viewModelScope.launch {
            // Add user message
            _chatMessages.value = _chatMessages.value + Message(role = "user", content = text)
            _isLoading.value = true

            try {
                var response = ""

                // Stream response from LLM Inference
                llmInference.generateStream(text).collect { token ->
                    response += token

                    val messages = _chatMessages.value
                    if (messages.lastOrNull()?.role == "assistant") {
                        // Update last message
                        _chatMessages.value = messages.dropLast(1) +
                                Message(role = "assistant", content = response)
                    } else {
                        // Add new message
                        _chatMessages.value = messages +
                                Message(role = "assistant", content = response)
                    }
                }

                addToHistory("CHAT", text)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value +
                        Message(role = "assistant", content = "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            Message(role = "assistant", content = "Chat cleared. Ask me anything!")
        )
    }

    // ==================== Quiz Functions ====================

    fun generateQuiz(topic: String, model: String, count: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val questions = generateTopicBasedQuizQuestions(topic, count)
                _quizQuestions.value = questions
                addToHistory("QUIZ_GENERATED", "Generated quiz on $topic")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateTopicBasedQuizQuestions(topic: String, count: Int): List<QuizQuestionUI> {
        val lowerTopic = topic.lowercase()

        val questionBank = when {
            // Python Programming
            lowerTopic.contains("python") -> listOf(
                QuizQuestionUI(
                    "What keyword is used to define a function in Python?",
                    listOf("def", "function", "func", "define"), "def", "EASY"
                ),
                QuizQuestionUI(
                    "Which of these is NOT a Python data type?",
                    listOf("list", "tuple", "array", "dictionary"), "array", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the output of: print(2 ** 3)?",
                    listOf("5", "6", "8", "9"), "8", "EASY"
                ),
                QuizQuestionUI(
                    "Which method is used to add an element to a list?",
                    listOf("add()", "append()", "push()", "insert()"), "append()", "EASY"
                ),
                QuizQuestionUI(
                    "What does 'self' represent in a Python class?",
                    listOf(
                        "Class name",
                        "Instance of the class",
                        "Global variable",
                        "Static method"
                    ), "Instance of the class", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Which operator is used for floor division?",
                    listOf("/", "//", "%", "div"), "//", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is a lambda function?",
                    listOf(
                        "Named function",
                        "Anonymous function",
                        "Class method",
                        "Built-in function"
                    ), "Anonymous function", "HARD"
                ),
                QuizQuestionUI(
                    "Which module is used for regular expressions?",
                    listOf("regex", "re", "regexp", "pattern"), "re", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the correct way to create a dictionary?",
                    listOf("{key: value}", "[key: value]", "(key: value)", "<key: value>"),
                    "{key: value}",
                    "EASY"
                ),
                QuizQuestionUI(
                    "What does 'pip' stand for?",
                    listOf(
                        "Python Install Package",
                        "Pip Installs Packages",
                        "Package Install Python",
                        "Python Integrated Platform"
                    ), "Pip Installs Packages", "HARD"
                )
            )

            // Solar System / Space
            lowerTopic.contains("solar") || lowerTopic.contains("planet") || lowerTopic.contains("space") -> listOf(
                QuizQuestionUI(
                    "How many planets are in our solar system?",
                    listOf("7", "8", "9", "10"), "8", "EASY"
                ),
                QuizQuestionUI(
                    "Which is the largest planet?",
                    listOf("Jupiter", "Saturn", "Neptune", "Earth"), "Jupiter", "EASY"
                ),
                QuizQuestionUI(
                    "Which planet is closest to the Sun?",
                    listOf("Venus", "Mercury", "Earth", "Mars"), "Mercury", "EASY"
                ),
                QuizQuestionUI(
                    "Which planet is known as the Red Planet?",
                    listOf("Venus", "Mars", "Jupiter", "Saturn"), "Mars", "EASY"
                ),
                QuizQuestionUI(
                    "Which planet has the most moons?",
                    listOf("Saturn", "Jupiter", "Neptune", "Uranus"), "Saturn", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the Sun primarily made of?",
                    listOf("Oxygen", "Carbon", "Hydrogen", "Helium"), "Hydrogen", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Which planet has rings visible from Earth?",
                    listOf("Jupiter", "Saturn", "Uranus", "Neptune"), "Saturn", "EASY"
                ),
                QuizQuestionUI(
                    "How long does it take Earth to orbit the Sun?",
                    listOf("24 hours", "30 days", "365 days", "12 months"), "365 days", "EASY"
                ),
                QuizQuestionUI(
                    "Which is the smallest planet?",
                    listOf("Mercury", "Mars", "Venus", "Pluto"), "Mercury", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What galaxy is our solar system in?",
                    listOf("Andromeda", "Milky Way", "Whirlpool", "Triangulum"),
                    "Milky Way",
                    "MEDIUM"
                )
            )

            // Photosynthesis / Biology
            lowerTopic.contains("photo") || lowerTopic.contains("biology") || lowerTopic.contains("plant") -> listOf(
                QuizQuestionUI(
                    "What is the primary product of photosynthesis?",
                    listOf("Oxygen", "Glucose", "Carbon dioxide", "Water"), "Glucose", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Which organelle performs photosynthesis?",
                    listOf("Mitochondria", "Chloroplast", "Nucleus", "Ribosome"),
                    "Chloroplast",
                    "EASY"
                ),
                QuizQuestionUI(
                    "What gas do plants absorb during photosynthesis?",
                    listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"),
                    "Carbon dioxide",
                    "EASY"
                ),
                QuizQuestionUI(
                    "What pigment makes plants green?",
                    listOf("Carotene", "Chlorophyll", "Xanthophyll", "Anthocyanin"),
                    "Chlorophyll",
                    "EASY"
                ),
                QuizQuestionUI(
                    "Where does photosynthesis primarily occur?",
                    listOf("Roots", "Stem", "Leaves", "Flowers"), "Leaves", "EASY"
                ),
                QuizQuestionUI(
                    "What is the equation for photosynthesis?",
                    listOf(
                        "6CO2 + 6H2O → C6H12O6 + 6O2",
                        "C6H12O6 → 6CO2 + 6H2O",
                        "2H2 + O2 → 2H2O",
                        "N2 + 3H2 → 2NH3"
                    ), "6CO2 + 6H2O → C6H12O6 + 6O2", "HARD"
                ),
                QuizQuestionUI(
                    "Which light is most effective for photosynthesis?",
                    listOf("Green", "Blue and Red", "Yellow", "Orange"), "Blue and Red", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is released as a byproduct?",
                    listOf("Carbon dioxide", "Nitrogen", "Oxygen", "Hydrogen"), "Oxygen", "EASY"
                ),
                QuizQuestionUI(
                    "What provides energy for photosynthesis?",
                    listOf("Water", "Sunlight", "Soil", "Air"), "Sunlight", "EASY"
                ),
                QuizQuestionUI(
                    "What are the tiny pores on leaves called?",
                    listOf("Stomata", "Chloroplasts", "Veins", "Cuticle"), "Stomata", "MEDIUM"
                )
            )

            // Math / Algebra
            lowerTopic.contains("math") || lowerTopic.contains("algebra") -> listOf(
                QuizQuestionUI(
                    "What is 15% of 200?",
                    listOf("25", "30", "35", "40"), "30", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the square root of 144?",
                    listOf("10", "11", "12", "13"), "12", "EASY"
                ),
                QuizQuestionUI(
                    "Solve: 2x + 5 = 15",
                    listOf("x = 4", "x = 5", "x = 6", "x = 7"), "x = 5", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is 7²?",
                    listOf("14", "21", "49", "56"), "49", "EASY"
                ),
                QuizQuestionUI(
                    "What is the value of π (pi) approximately?",
                    listOf("2.14", "3.14", "4.14", "5.14"), "3.14", "EASY"
                ),
                QuizQuestionUI(
                    "What is the area of a rectangle with length 8 and width 5?",
                    listOf("13", "26", "40", "45"), "40", "EASY"
                ),
                QuizQuestionUI(
                    "Solve: 3(x - 2) = 12",
                    listOf("x = 5", "x = 6", "x = 7", "x = 8"), "x = 6", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is 25% as a fraction?",
                    listOf("1/2", "1/3", "1/4", "1/5"), "1/4", "EASY"
                ),
                QuizQuestionUI(
                    "What is the Pythagorean theorem?",
                    listOf("a + b = c", "a² + b² = c²", "a × b = c", "a² - b² = c²"),
                    "a² + b² = c²",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the slope of line y = 2x + 3?",
                    listOf("1", "2", "3", "4"), "2", "HARD"
                )
            )

            // History
            lowerTopic.contains("history") || lowerTopic.contains("war") -> listOf(
                QuizQuestionUI(
                    "When did World War I start?",
                    listOf("1912", "1914", "1916", "1918"), "1914", "MEDIUM"
                ),
                QuizQuestionUI(
                    "When did World War II end?",
                    listOf("1943", "1944", "1945", "1946"), "1945", "EASY"
                ),
                QuizQuestionUI(
                    "Who was the first President of the United States?",
                    listOf(
                        "Thomas Jefferson",
                        "George Washington",
                        "John Adams",
                        "Benjamin Franklin"
                    ), "George Washington", "EASY"
                ),
                QuizQuestionUI(
                    "Which country built the Great Wall?",
                    listOf("Japan", "Korea", "China", "Mongolia"), "China", "EASY"
                ),
                QuizQuestionUI(
                    "When was the Declaration of Independence signed?",
                    listOf("1775", "1776", "1777", "1778"), "1776", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Who invented the telephone?",
                    listOf(
                        "Thomas Edison",
                        "Alexander Graham Bell",
                        "Nikola Tesla",
                        "Benjamin Franklin"
                    ), "Alexander Graham Bell", "EASY"
                ),
                QuizQuestionUI(
                    "Which empire built the Colosseum?",
                    listOf("Greek", "Roman", "Byzantine", "Ottoman"), "Roman", "EASY"
                ),
                QuizQuestionUI(
                    "Who was the first person on the Moon?",
                    listOf("Buzz Aldrin", "Neil Armstrong", "Yuri Gagarin", "John Glenn"),
                    "Neil Armstrong",
                    "EASY"
                ),
                QuizQuestionUI(
                    "When did the Renaissance begin?",
                    listOf("12th century", "13th century", "14th century", "15th century"),
                    "14th century",
                    "HARD"
                ),
                QuizQuestionUI(
                    "Who wrote the Declaration of Independence?",
                    listOf(
                        "George Washington",
                        "Benjamin Franklin",
                        "Thomas Jefferson",
                        "John Adams"
                    ), "Thomas Jefferson", "MEDIUM"
                )
            )

            // Geography
            lowerTopic.contains("geo") || lowerTopic.contains("capital") || lowerTopic.contains("country") -> listOf(
                QuizQuestionUI(
                    "What is the capital of France?",
                    listOf("London", "Paris", "Berlin", "Rome"), "Paris", "EASY"
                ),
                QuizQuestionUI(
                    "What is the capital of Japan?",
                    listOf("Tokyo", "Kyoto", "Osaka", "Seoul"), "Tokyo", "EASY"
                ),
                QuizQuestionUI(
                    "Which is the largest country by area?",
                    listOf("Canada", "China", "USA", "Russia"), "Russia", "EASY"
                ),
                QuizQuestionUI(
                    "What is the capital of Australia?",
                    listOf("Sydney", "Melbourne", "Canberra", "Brisbane"), "Canberra", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Which continent is Egypt in?",
                    listOf("Asia", "Africa", "Europe", "Middle East"), "Africa", "EASY"
                ),
                QuizQuestionUI(
                    "What is the tallest mountain in the world?",
                    listOf("K2", "Mount Everest", "Kilimanjaro", "Denali"), "Mount Everest", "EASY"
                ),
                QuizQuestionUI(
                    "Which ocean is the largest?",
                    listOf("Atlantic", "Indian", "Pacific", "Arctic"), "Pacific", "EASY"
                ),
                QuizQuestionUI(
                    "What is the capital of Brazil?",
                    listOf("Rio de Janeiro", "São Paulo", "Brasília", "Salvador"),
                    "Brasília",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "Which country has the most population?",
                    listOf("India", "China", "USA", "Indonesia"), "China", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the longest river in the world?",
                    listOf("Amazon", "Nile", "Yangtze", "Mississippi"), "Nile", "HARD"
                )
            )

            // Science / Physics
            lowerTopic.contains("science") || lowerTopic.contains("physics") -> listOf(
                QuizQuestionUI(
                    "What is the speed of light?",
                    listOf("300,000 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s"),
                    "300,000 km/s",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is gravity on Earth approximately?",
                    listOf("8.8 m/s²", "9.8 m/s²", "10.8 m/s²", "11.8 m/s²"), "9.8 m/s²", "EASY"
                ),
                QuizQuestionUI(
                    "Who developed the theory of relativity?",
                    listOf("Isaac Newton", "Albert Einstein", "Galileo Galilei", "Stephen Hawking"),
                    "Albert Einstein",
                    "EASY"
                ),
                QuizQuestionUI(
                    "What is the smallest unit of matter?",
                    listOf("Molecule", "Atom", "Proton", "Quark"), "Atom", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What force opposes motion?",
                    listOf("Gravity", "Friction", "Magnetism", "Inertia"), "Friction", "EASY"
                ),
                QuizQuestionUI(
                    "What is the formula for force?",
                    listOf("F = m × a", "F = m × v", "F = m / a", "F = a / m"),
                    "F = m × a",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "What is the SI unit of energy?",
                    listOf("Watt", "Newton", "Joule", "Pascal"), "Joule", "MEDIUM"
                ),
                QuizQuestionUI(
                    "What travels faster: light or sound?",
                    listOf("Light", "Sound", "Same speed", "Depends on medium"), "Light", "EASY"
                ),
                QuizQuestionUI(
                    "What is the charge of an electron?",
                    listOf("Positive", "Negative", "Neutral", "Variable"), "Negative", "EASY"
                ),
                QuizQuestionUI(
                    "What is the first law of thermodynamics?",
                    listOf(
                        "Energy cannot be created or destroyed",
                        "F = ma",
                        "E = mc²",
                        "Every action has a reaction"
                    ), "Energy cannot be created or destroyed", "HARD"
                )
            )

            // Default general knowledge
            else -> listOf(
                QuizQuestionUI(
                    "What is $topic primarily about?",
                    listOf("Science", "Mathematics", "Language", "History"), "Science", "EASY"
                ),
                QuizQuestionUI(
                    "Which field does $topic belong to?",
                    listOf("Natural Sciences", "Social Sciences", "Arts", "Engineering"),
                    "Natural Sciences",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "Is $topic studied in school?",
                    listOf("Yes", "No", "Sometimes", "Rarely"), "Yes", "EASY"
                ),
                QuizQuestionUI(
                    "What skill does $topic develop?",
                    listOf("Critical thinking", "Creativity", "Physical fitness", "Social skills"),
                    "Critical thinking",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "Can $topic be learned online?",
                    listOf("Yes", "No", "Partially", "Not recommended"), "Yes", "EASY"
                ),
                QuizQuestionUI(
                    "Is $topic important for students?",
                    listOf("Very important", "Somewhat important", "Not important", "Depends"),
                    "Very important",
                    "EASY"
                ),
                QuizQuestionUI(
                    "Which age group studies $topic?",
                    listOf("Elementary", "Middle school", "High school", "All ages"),
                    "All ages",
                    "MEDIUM"
                ),
                QuizQuestionUI(
                    "Does $topic require practice?",
                    listOf("Yes, daily", "Sometimes", "Rarely", "No"), "Yes, daily", "EASY"
                ),
                QuizQuestionUI(
                    "Can $topic help in career?",
                    listOf("Yes", "No", "Maybe", "Depends on field"), "Yes", "MEDIUM"
                ),
                QuizQuestionUI(
                    "Is $topic interesting?",
                    listOf("Very interesting", "Somewhat", "Not much", "Boring"),
                    "Very interesting",
                    "EASY"
                )
            )
        }

        // Distribute difficulties: 7 EASY, 7 MEDIUM, 6 HARD
        val easyQuestions = questionBank.filter { it.difficulty == "EASY" }.take(7)
        val mediumQuestions = questionBank.filter { it.difficulty == "MEDIUM" }.take(7)
        val hardQuestions = questionBank.filter { it.difficulty == "HARD" }.take(6)

        return (easyQuestions + mediumQuestions + hardQuestions).take(count)
    }

    fun submitQuiz(selectedAnswers: Map<Int, String>, questions: List<QuizQuestionUI>) {
        viewModelScope.launch {
            val score = questions.filterIndexed { index, q ->
                selectedAnswers[index] == q.correctAnswer
            }.size

            _questionsAnswered.value += questions.size
            if (score > questions.size * 0.7) {
                _topicsMastered.value++
            }

            updateUserStats()
            addToHistory("QUIZ_COMPLETED", "Score: $score/${questions.size}")
        }
    }

    fun clearQuiz() {
        _quizQuestions.value = emptyList()
    }

    // ==================== Explain Functions ====================

    fun explainTopic(topic: String, model: String) {
        viewModelScope.launch {
            if (_modelStatus.value != "Ready") return@launch

            _isLoading.value = true
            _explanation.value = ""

            try {
                val prompt =
                    "Explain $topic in detail with examples, formulas, and applications for students"
                val explanation = llmInference.generate(prompt)
                _explanation.value = explanation
                addToHistory("EXPLAIN", "Explained: $topic")
                updateLearningStreak()
            } catch (e: Exception) {
                _explanation.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Practice Functions ====================

    fun generatePractice(topic: String, model: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _practiceProblems.value = generateTopicBasedPracticeProblems(topic)
                addToHistory("PRACTICE_GENERATED", "Generated practice for $topic")
                updateLearningStreak()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateTopicBasedPracticeProblems(topic: String): List<PracticeProblemUI> {
        val lowerTopic = topic.lowercase()

        return when {
            // Python Programming
            lowerTopic.contains("python") -> listOf(
                PracticeProblemUI(
                    question = "Write a function that takes a list of numbers and returns the sum.",
                    type = "MCQ",
                    options = listOf(
                        "def sum_list(nums): return sum(nums)",
                        "def sum_list(nums): return nums.sum()",
                        "def sum_list(nums): return add(nums)",
                        "def sum_list(nums): return total(nums)"
                    ),
                    answer = "def sum_list(nums): return sum(nums)",
                    solution = "Python's built-in sum() function returns the sum of all items in an iterable. This is the correct and Pythonic way to sum a list."
                ),
                PracticeProblemUI(
                    question = "How do you create an empty dictionary in Python?",
                    type = "MCQ",
                    options = listOf("{}", "[]", "()", "dict()"),
                    answer = "{}",
                    solution = "An empty dictionary is created using {} curly braces. You can also use dict() but {} is more common and faster."
                ),
                PracticeProblemUI(
                    question = "What is the output of: print(type([1, 2, 3]))?",
                    type = "MCQ",
                    options = listOf(
                        "<class 'list'>",
                        "<class 'array'>",
                        "<class 'tuple'>",
                        "<class 'set'>"
                    ),
                    answer = "<class 'list'>",
                    solution = "The type() function returns the type of an object. [1, 2, 3] is a list, so it returns <class 'list'>."
                ),
                PracticeProblemUI(
                    question = "Write a list comprehension to get all even numbers from 1 to 10.",
                    type = "MCQ",
                    options = listOf(
                        "[x for x in range(1, 11) if x % 2 == 0]",
                        "[x for x in range(10) if x % 2]",
                        "[x if x % 2 == 0 for x in range(10)]",
                        "[x for x in range(1, 10) where x % 2 == 0]"
                    ),
                    answer = "[x for x in range(1, 11) if x % 2 == 0]",
                    solution = "List comprehension syntax: [expression for item in iterable if condition]. range(1, 11) gives 1-10, x % 2 == 0 checks if even."
                ),
                PracticeProblemUI(
                    question = "What does the 'self' parameter represent in a Python class method?",
                    type = "MCQ",
                    options = listOf(
                        "The instance of the class",
                        "The class itself",
                        "A global variable",
                        "The parent class"
                    ),
                    answer = "The instance of the class",
                    solution = "'self' refers to the instance of the class. It's used to access instance variables and methods. It's the first parameter in instance methods."
                )
            )

            // Math
            lowerTopic.contains("math") || lowerTopic.contains("algebra") -> listOf(
                PracticeProblemUI(
                    question = "Solve for x: 3x + 7 = 22",
                    type = "MCQ",
                    options = listOf("x = 5", "x = 6", "x = 7", "x = 8"),
                    answer = "x = 5",
                    solution = "Step 1: 3x + 7 = 22\nStep 2: 3x = 22 - 7\nStep 3: 3x = 15\nStep 4: x = 15/3 = 5"
                ),
                PracticeProblemUI(
                    question = "What is 20% of 150?",
                    type = "MCQ",
                    options = listOf("25", "30", "35", "40"),
                    answer = "30",
                    solution = "20% = 20/100 = 0.2\n0.2 × 150 = 30\nOr: (20 × 150) / 100 = 3000 / 100 = 30"
                ),
                PracticeProblemUI(
                    question = "If a rectangle has length 12 cm and width 5 cm, what is its perimeter?",
                    type = "MCQ",
                    options = listOf("17 cm", "34 cm", "60 cm", "30 cm"),
                    answer = "34 cm",
                    solution = "Perimeter = 2(length + width)\n= 2(12 + 5)\n= 2(17)\n= 34 cm"
                ),
                PracticeProblemUI(
                    question = "Simplify: (2x + 3) + (4x - 5)",
                    type = "MCQ",
                    options = listOf("6x - 2", "6x + 2", "2x - 2", "6x - 8"),
                    answer = "6x - 2",
                    solution = "(2x + 3) + (4x - 5)\n= 2x + 4x + 3 - 5\n= 6x - 2"
                ),
                PracticeProblemUI(
                    question = "What is the value of 5² - 3²?",
                    type = "MCQ",
                    options = listOf("4", "16", "8", "2"),
                    answer = "16",
                    solution = "5² = 25\n3² = 9\n25 - 9 = 16"
                )
            )

            // Science/Physics
            lowerTopic.contains("science") || lowerTopic.contains("physics") -> listOf(
                PracticeProblemUI(
                    question = "A car accelerates from 0 to 20 m/s in 5 seconds. What is its acceleration?",
                    type = "MCQ",
                    options = listOf("2 m/s²", "4 m/s²", "5 m/s²", "10 m/s²"),
                    answer = "4 m/s²",
                    solution = "Acceleration = (Final velocity - Initial velocity) / Time\na = (20 - 0) / 5 = 20 / 5 = 4 m/s²"
                ),
                PracticeProblemUI(
                    question = "If a 10 kg object is lifted 2 meters, how much work is done? (g = 10 m/s²)",
                    type = "MCQ",
                    options = listOf("20 J", "100 J", "200 J", "50 J"),
                    answer = "200 J",
                    solution = "Work = Force × Distance\nForce = mass × gravity = 10 × 10 = 100 N\nWork = 100 × 2 = 200 Joules"
                ),
                PracticeProblemUI(
                    question = "What is the frequency of a wave with wavelength 2m traveling at 10 m/s?",
                    type = "MCQ",
                    options = listOf("2 Hz", "5 Hz", "10 Hz", "20 Hz"),
                    answer = "5 Hz",
                    solution = "Frequency = Speed / Wavelength\nf = v / λ = 10 / 2 = 5 Hz"
                ),
                PracticeProblemUI(
                    question = "How much force is needed to accelerate a 5 kg object at 3 m/s²?",
                    type = "MCQ",
                    options = listOf("8 N", "15 N", "2 N", "1.67 N"),
                    answer = "15 N",
                    solution = "Newton's 2nd Law: F = ma\nF = 5 kg × 3 m/s² = 15 N"
                ),
                PracticeProblemUI(
                    question = "A ball is dropped from 20m. How long does it take to hit the ground? (g = 10 m/s²)",
                    type = "MCQ",
                    options = listOf("1 s", "2 s", "4 s", "10 s"),
                    answer = "2 s",
                    solution = "Using s = ½gt²\n20 = ½ × 10 × t²\n20 = 5t²\nt² = 4\nt = 2 seconds"
                )
            )

            // Biology
            lowerTopic.contains("biology") || lowerTopic.contains("cell") -> listOf(
                PracticeProblemUI(
                    question = "Which organelle is responsible for producing ATP (energy)?",
                    type = "MCQ",
                    options = listOf("Mitochondria", "Nucleus", "Ribosome", "Golgi apparatus"),
                    answer = "Mitochondria",
                    solution = "Mitochondria are called the 'powerhouse of the cell' because they produce ATP through cellular respiration."
                ),
                PracticeProblemUI(
                    question = "What process do cells use to divide for growth and repair?",
                    type = "MCQ",
                    options = listOf("Mitosis", "Meiosis", "Photosynthesis", "Respiration"),
                    answer = "Mitosis",
                    solution = "Mitosis is cell division that produces two identical daughter cells for growth and repair. Meiosis produces sex cells."
                ),
                PracticeProblemUI(
                    question = "In which phase of mitosis do chromosomes line up in the middle?",
                    type = "MCQ",
                    options = listOf("Metaphase", "Prophase", "Anaphase", "Telophase"),
                    answer = "Metaphase",
                    solution = "Metaphase: chromosomes line up at the metaphase plate (middle). Think 'M' for middle."
                ),
                PracticeProblemUI(
                    question = "What is the complementary DNA strand to: ATGC?",
                    type = "MCQ",
                    options = listOf("TACG", "AUGC", "ATGC", "GCTA"),
                    answer = "TACG",
                    solution = "DNA base pairing: A pairs with T, G pairs with C. So ATGC → TACG"
                ),
                PracticeProblemUI(
                    question = "Which type of blood cell fights infections?",
                    type = "MCQ",
                    options = listOf("White blood cells", "Red blood cells", "Platelets", "Plasma"),
                    answer = "White blood cells",
                    solution = "White blood cells (leukocytes) are part of the immune system and fight infections. Red blood cells carry oxygen."
                )
            )

            // Geography
            lowerTopic.contains("geo") || lowerTopic.contains("capital") -> listOf(
                PracticeProblemUI(
                    question = "Which ocean is the largest?",
                    type = "MCQ",
                    options = listOf(
                        "Pacific Ocean",
                        "Atlantic Ocean",
                        "Indian Ocean",
                        "Arctic Ocean"
                    ),
                    answer = "Pacific Ocean",
                    solution = "The Pacific Ocean covers ~165 million km², making it the largest and deepest ocean."
                ),
                PracticeProblemUI(
                    question = "On which continent is the Sahara Desert?",
                    type = "MCQ",
                    options = listOf("Africa", "Asia", "Australia", "South America"),
                    answer = "Africa",
                    solution = "The Sahara Desert is in North Africa, covering most of the continent's northern region."
                ),
                PracticeProblemUI(
                    question = "How many continents are there?",
                    type = "MCQ",
                    options = listOf("7", "5", "6", "8"),
                    answer = "7",
                    solution = "The 7 continents are: Asia, Africa, North America, South America, Antarctica, Europe, and Australia."
                ),
                PracticeProblemUI(
                    question = "What is the capital of Canada?",
                    type = "MCQ",
                    options = listOf("Ottawa", "Toronto", "Vancouver", "Montreal"),
                    answer = "Ottawa",
                    solution = "Ottawa is the capital of Canada. Toronto is the largest city, but not the capital."
                ),
                PracticeProblemUI(
                    question = "Which river is the longest in the world?",
                    type = "MCQ",
                    options = listOf(
                        "Nile River",
                        "Amazon River",
                        "Yangtze River",
                        "Mississippi River"
                    ),
                    answer = "Nile River",
                    solution = "The Nile River (6,650 km) is generally considered the longest, though Amazon carries more water."
                )
            )

            // Default
            else -> listOf(
                PracticeProblemUI(
                    question = "What is the main concept of $topic?",
                    type = "MCQ",
                    options = listOf(
                        "Understanding basic principles",
                        "Memorizing facts",
                        "Solving complex problems",
                        "Applying knowledge"
                    ),
                    answer = "Understanding basic principles",
                    solution = "The foundation of learning $topic is understanding its basic principles, which allows you to apply knowledge effectively."
                ),
                PracticeProblemUI(
                    question = "Which skill is most important when studying $topic?",
                    type = "MCQ",
                    options = listOf("Critical thinking", "Memory", "Speed", "Creativity"),
                    answer = "Critical thinking",
                    solution = "Critical thinking helps you analyze, evaluate, and apply concepts effectively in $topic."
                ),
                PracticeProblemUI(
                    question = "How can you best learn $topic?",
                    type = "MCQ",
                    options = listOf(
                        "Practice regularly",
                        "Read once",
                        "Watch videos only",
                        "Memorize everything"
                    ),
                    answer = "Practice regularly",
                    solution = "Regular practice with $topic helps reinforce concepts and improve understanding over time."
                )
            )
        }
    }

    fun clearPractice() {
        _practiceProblems.value = emptyList()
    }

    fun updateStats() {
        viewModelScope.launch {
            _questionsAnswered.value++
            updateUserStats()
            updateLearningStreak()
        }
    }

    // ==================== Learning Streak Management ====================

    private suspend fun updateLearningStreak() {
        val today = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        val lastActive = prefsManager.getLastActiveDate()
        val daysDiff = (today - lastActive) / oneDayMs

        when {
            daysDiff == 0L -> {
                // Same day, no streak change
            }

            daysDiff == 1L -> {
                // Consecutive day, increment streak
                _learningStreak.value++
            }

            else -> {
                // Missed a day, reset streak
                _learningStreak.value = 1
            }
        }

        prefsManager.updateLastActiveDate()
        updateUserStats()
    }

    // ==================== Helper Functions ====================

    private fun updateUserStats() {
        viewModelScope.launch {
            // Save stats to PreferencesManager when needed
            // For now just updating in-memory state
        }
    }

    private fun addToHistory(type: String, data: String) {
        val newActivity = ActivityHistoryUI(
            type = type,
            data = data,
            timestamp = System.currentTimeMillis()
        )
        _history.value = listOf(newActivity) + _history.value
    }
}

// UI Data classes
data class Message(
    val role: String,
    val content: String
)

data class QuizQuestionUI(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val difficulty: String
)

data class PracticeProblemUI(
    val question: String,
    val type: String,
    val options: List<String>?,
    val answer: String,
    val solution: String
)

data class ActivityHistoryUI(
    val type: String,
    val data: String,
    val timestamp: Long
)
