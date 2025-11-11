package com.aithink.studycompanion.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aithink.studycompanion.data.local.PreferencesManager
import com.aithink.studycompanion.data.models.LearningPurpose
import com.aithink.studycompanion.data.models.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    preferencesManager: PreferencesManager,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(preferencesManager))
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var selectedPurpose by remember { mutableStateOf<LearningPurpose?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎓 AiThink",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "AI Study Companion",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Your Role", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = selectedRole == UserRole.STUDENT,
                onClick = { selectedRole = UserRole.STUDENT },
                label = { Text("Student") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            FilterChip(
                selected = selectedRole == UserRole.IT_PROFESSIONAL,
                onClick = { selectedRole = UserRole.IT_PROFESSIONAL },
                label = { Text("IT Professional") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedPurpose?.name?.replace("_", " ") ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Learning Purpose") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                LearningPurpose.values().forEach { purpose ->
                    DropdownMenuItem(
                        text = { Text(purpose.name.replace("_", " ")) },
                        onClick = {
                            selectedPurpose = purpose
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() &&
                    selectedRole != null && selectedPurpose != null
                ) {
                    viewModel.saveProfile(name, email, selectedRole!!, selectedPurpose!!)
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Start Learning", style = MaterialTheme.typography.titleMedium)
        }
    }
}
