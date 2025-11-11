package com.aithink.studycompanion.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    var selectedModel by remember { mutableStateOf("gemma3:1b") }
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Learning Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Stats Grid
        StatsGrid(viewModel)

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("💬 Chat") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📝 Quiz") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("💡 Explain") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("🎯 Practice") }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("📈 Progress") }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> ChatTab(viewModel, selectedModel)
            1 -> QuizTab(viewModel, selectedModel)
            2 -> ExplainTab(viewModel, selectedModel)
            3 -> PracticeTab(viewModel, selectedModel)
            4 -> ProgressTab(viewModel)
        }
    }
}

@Composable
fun StatsGrid(viewModel: DashboardViewModel) {
    val streak by viewModel.learningStreak.collectAsState()
    val questions by viewModel.questionsAnswered.collectAsState()
    val topics by viewModel.topicsMastered.collectAsState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                icon = "🔥",
                label = "Learning Streak",
                value = "$streak days",
                gradient = listOf(Color(0xFFEA580C), Color(0xFFDC2626))
            )
        }
        item {
            StatCard(
                icon = "⭐",
                label = "Topics Mastered",
                value = "$topics",
                gradient = listOf(Color(0xFFCA8A04), Color(0xFFEA580C))
            )
        }
        item {
            StatCard(
                icon = "✓",
                label = "Questions Answered",
                value = "$questions",
                gradient = listOf(Color(0xFF16A34A), Color(0xFF059669))
            )
        }
    }
}

@Composable
fun StatCard(
    icon: String,
    label: String,
    value: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradient)
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTab(viewModel: DashboardViewModel, model: String) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val modelStatus by viewModel.modelStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Model Status Banner
        if (modelStatus != "Ready") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        modelStatus.contains("Error") || modelStatus.contains("Failed") -> Color(
                            0xFFFEE2E2
                        )

                        modelStatus.contains("Download") -> Color(0xFFDCFCE7)
                        else -> Color(0xFFFFF3CD)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!modelStatus.contains("Error") && !modelStatus.contains("Failed")) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = when {
                                    modelStatus.contains("Download") -> Color(0xFF16A34A)
                                    else -> Color(0xFFEA580C)
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                modelStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    modelStatus.contains("Error") || modelStatus.contains("Failed") -> Color(
                                        0xFF991B1B
                                    )

                                    modelStatus.contains("Download") -> Color(0xFF166534)
                                    else -> Color(0xFF78350F)
                                }
                            )
                            if (modelStatus.contains("Download") && downloadProgress > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$downloadProgress% complete",
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    if (modelStatus.contains("Download") && downloadProgress > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = downloadProgress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty() && modelStatus == "Ready") {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 100.dp)
                        ) {
                            Text(
                                "💬",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Start a conversation...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            items(messages) { message ->
                MessageBubble(message)
            }

            if (isLoading && messages.lastOrNull()?.role != "assistant") {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "AI thinking...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything (offline)...") },
                enabled = !isLoading && modelStatus == "Ready"
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText, model)
                        inputText = ""
                    }
                },
                enabled = !isLoading && modelStatus == "Ready" && inputText.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.role == "user")
                    Color(0xFF3B82F6)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.role == "user")
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizTab(viewModel: DashboardViewModel, model: String) {
    var topic by remember { mutableStateOf("") }
    val questions by viewModel.quizQuestions.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, String>() }
    var showResults by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    if (questions.isEmpty()) {
        // Input Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📝",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Generate Quiz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Enter topic (e.g., Python, Solar System)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateQuiz(topic, model, 20) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && topic.isNotBlank()
            ) {
                Text(
                    text = if (isLoading) "Generating 20 Questions..." else "Generate Quiz",
                    fontSize = 16.sp
                )
            }
        }
    } else {
        // Quiz Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Progress
            LinearProgressIndicator(
                progress = (currentIndex + 1) / questions.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            val question = questions[currentIndex]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Question ${currentIndex + 1} of ${questions.size}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Show difficulty badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (question.difficulty) {
                            "EASY" -> Color(0xFF16A34A)
                            "MEDIUM" -> Color(0xFFEA580C)
                            "HARD" -> Color(0xFFDC2626)
                            else -> Color.Gray
                        }
                    )
                ) {
                    Text(
                        text = question.difficulty,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = question.question,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                question.options.forEach { option ->
                    val isSelected = selectedAnswers[currentIndex] == option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAnswers[currentIndex] = option
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                Color(0xFF3B82F6)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { currentIndex = maxOf(0, currentIndex - 1) },
                    enabled = currentIndex > 0
                ) {
                    Text("Previous")
                }

                if (currentIndex == questions.size - 1) {
                    Button(
                        onClick = {
                            showResults = true
                            viewModel.submitQuiz(selectedAnswers.toMap(), questions)
                        }
                    ) {
                        Text("Submit Quiz")
                    }
                } else {
                    Button(onClick = { currentIndex++ }) {
                        Text("Next")
                    }
                }
            }

            if (showResults) {
                val score = questions.filterIndexed { index, q ->
                    selectedAnswers[index] == q.correctAnswer
                }.size

                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Quiz Completed! 🎉") },
                    text = {
                        Column {
                            Text(
                                "Score: $score / ${questions.size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Percentage: ${(score * 100 / questions.size)}%",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Evaluated by: $model",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.clearQuiz()
                            showResults = false
                            currentIndex = 0
                            selectedAnswers.clear()
                        }) {
                            Text("New Quiz")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExplainTab(viewModel: DashboardViewModel, model: String) {
    var topic by remember { mutableStateOf("") }
    val explanation by viewModel.explanation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Enter topic to explain...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = { viewModel.explainTopic(topic, model) },
                enabled = !isLoading && topic.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Text(if (isLoading) "..." else "Explain")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (explanation.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = explanation,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "💡",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Enter a topic to get a detailed explanation",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeTab(viewModel: DashboardViewModel, model: String) {
    var topic by remember { mutableStateOf("") }
    val problems by viewModel.practiceProblems.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    var userAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var showSolution by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    val isLoading by viewModel.isLoading.collectAsState()

    if (problems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎯",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Practice Problems",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Enter topic for practice...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generatePractice(topic, model) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && topic.isNotBlank()
            ) {
                Text(
                    text = if (isLoading) "Generating..." else "Generate Practice Problems",
                    fontSize = 16.sp
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Problem ${currentIndex + 1} of ${problems.size}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val problem = problems[currentIndex]

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = problem.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Answer Input
            if (problem.type == "MCQ" && problem.options != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    problem.options.forEach { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userAnswers[currentIndex] = option
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (userAnswers[currentIndex] == option)
                                    Color(0xFF3B82F6)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(16.dp),
                                color = if (userAnswers[currentIndex] == option)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = userAnswers[currentIndex] ?: "",
                    onValueChange = { userAnswers[currentIndex] = it },
                    label = { Text("Type your answer...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    maxLines = 5
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showSolution[currentIndex] = true
                    viewModel.updateStats()
                },
                enabled = userAnswers[currentIndex]?.isNotBlank() == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Answer")
            }

            if (showSolution[currentIndex] == true) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val isCorrect = userAnswers[currentIndex] == problem.answer
                        Text(
                            text = if (isCorrect) "✓ Correct!" else "✗ Incorrect",
                            color = if (isCorrect) Color(0xFF16A34A) else Color(0xFFEA580C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Correct Answer: ${problem.answer}",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            problem.solution,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Evaluated by: $model",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { currentIndex = maxOf(0, currentIndex - 1) },
                    enabled = currentIndex > 0
                ) {
                    Text("Previous")
                }

                OutlinedButton(
                    onClick = { currentIndex = minOf(problems.size - 1, currentIndex + 1) },
                    enabled = currentIndex < problems.size - 1
                ) {
                    Text("Next")
                }

                Button(
                    onClick = {
                        viewModel.clearPractice()
                        currentIndex = 0
                        userAnswers.clear()
                        showSolution.clear()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEA580C)
                    )
                ) {
                    Text("New Topic")
                }
            }
        }
    }
}

@Composable
fun ProgressTab(viewModel: DashboardViewModel) {
    val streak by viewModel.learningStreak.collectAsState()
    val questions by viewModel.questionsAnswered.collectAsState()
    val topics by viewModel.topicsMastered.collectAsState()
    val history by viewModel.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = "🔥",
                label = "Learning Streak",
                value = "$streak days",
                gradient = listOf(Color(0xFFEA580C), Color(0xFFDC2626)),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = "✓",
                label = "Questions",
                value = "$questions",
                gradient = listOf(Color(0xFF16A34A), Color(0xFF059669)),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = "⭐",
                label = "Topics",
                value = "$topics",
                gradient = listOf(Color(0xFFCA8A04), Color(0xFFEA580C)),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = "📊",
                label = "Accuracy",
                value = "${if (questions > 0) (topics * 100 / questions).coerceAtMost(100) else 0}%",
                gradient = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Activity History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📚",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No activity yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start learning to see your progress!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            history.groupBy {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it.timestamp))
            }.forEach { (date, activities) ->
                Text(
                    date,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                activities.forEach { activity ->
                    ActivityCard(activity)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityHistoryUI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getActivityIcon(activity.type),
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.type.replace("_", " ").replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = activity.data,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date(activity.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

fun getActivityIcon(type: String): String {
    return when (type.uppercase()) {
        "CHAT" -> "💬"
        "QUIZ", "QUIZ_GENERATED" -> "📝"
        "QUIZ_COMPLETED" -> "✅"
        "EXPLAIN" -> "💡"
        "PRACTICE", "PRACTICE_GENERATED" -> "🎯"
        "PRACTICE_ANSWER" -> "✏️"
        else -> "📌"
    }
}
