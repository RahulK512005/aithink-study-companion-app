package com.aithink.studycompanion.ui.screens.subjects

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EducationLevel(
    val id: String,
    val name: String,
    val emoji: String,
    val ageRange: String
)

data class Subject(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val level: String
)

data class SubjectsUiState(
    val selectedLevel: EducationLevel? = null,
    val subjects: List<Subject> = emptyList(),
    val searchQuery: String = ""
)

class SubjectsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()
    
    val educationLevels = listOf(
        EducationLevel("lkg-ukg", "LKG-UKG", "🧸", "3-5 years"),
        EducationLevel("primary", "Primary (1-5)", "📚", "6-10 years"),
        EducationLevel("middle", "Middle (6-8)", "📖", "11-13 years"),
        EducationLevel("high", "High School (9-10)", "🎓", "14-15 years"),
        EducationLevel("senior", "Senior (11-12)", "🎯", "16-17 years"),
        EducationLevel("undergrad", "Undergraduate", "🎓", "18-22 years"),
        EducationLevel("postgrad", "Postgraduate", "🎓", "22-25 years"),
        EducationLevel("phd", "PhD/Research", "🔬", "25+ years")
    )
    
    private val allSubjects = mapOf(
        "lkg-ukg" to listOf(
            Subject("alphabets", "Alphabets", "🔤", "Learn A to Z", "lkg-ukg"),
            Subject("numbers", "Numbers", "🔢", "Count 1 to 100", "lkg-ukg"),
            Subject("colors", "Colors", "🎨", "Learn colors", "lkg-ukg"),
            Subject("shapes", "Shapes", "⭐", "Basic shapes", "lkg-ukg"),
            Subject("rhymes", "Rhymes", "🎵", "Fun songs", "lkg-ukg")
        ),
        "primary" to listOf(
            Subject("math", "Mathematics", "➕", "Basic arithmetic", "primary"),
            Subject("english", "English", "📝", "Reading & writing", "primary"),
            Subject("science", "Science", "🔬", "Nature & experiments", "primary"),
            Subject("social", "Social Studies", "🌍", "World around us", "primary"),
            Subject("art", "Art & Craft", "🎨", "Creative activities", "primary")
        ),
        "middle" to listOf(
            Subject("math", "Mathematics", "📐", "Algebra & geometry", "middle"),
            Subject("science", "Science", "🧪", "Physics, Chemistry, Biology", "middle"),
            Subject("english", "English", "📖", "Grammar & literature", "middle"),
            Subject("social", "Social Science", "🗺️", "History & geography", "middle"),
            Subject("computer", "Computer Science", "💻", "Basic programming", "middle")
        ),
        "high" to listOf(
            Subject("math", "Mathematics", "📊", "Advanced algebra & trigonometry", "high"),
            Subject("physics", "Physics", "⚡", "Mechanics & electricity", "high"),
            Subject("chemistry", "Chemistry", "⚗️", "Elements & reactions", "high"),
            Subject("biology", "Biology", "🧬", "Life sciences", "high"),
            Subject("english", "English", "📚", "Literature & composition", "high"),
            Subject("social", "Social Science", "🏛️", "Civics & economics", "high")
        ),
        "senior" to listOf(
            Subject("math", "Mathematics", "∫", "Calculus & statistics", "senior"),
            Subject("physics", "Physics", "🔭", "Modern physics", "senior"),
            Subject("chemistry", "Chemistry", "🧪", "Organic & inorganic", "senior"),
            Subject("biology", "Biology", "🦠", "Genetics & ecology", "senior"),
            Subject("cs", "Computer Science", "💾", "Programming & algorithms", "senior"),
            Subject("commerce", "Commerce", "💰", "Accounts & business", "senior"),
            Subject("economics", "Economics", "📈", "Micro & macro", "senior"),
            Subject("english", "English", "✍️", "Advanced literature", "senior")
        ),
        "undergrad" to listOf(
            Subject("engineering", "Engineering", "⚙️", "All branches", "undergrad"),
            Subject("medical", "Medical Sciences", "🏥", "MBBS & allied", "undergrad"),
            Subject("commerce", "Commerce & Business", "💼", "BBA, BCom", "undergrad"),
            Subject("science", "Pure Sciences", "🔬", "BSc programs", "undergrad"),
            Subject("arts", "Arts & Humanities", "🎭", "BA programs", "undergrad"),
            Subject("law", "Law", "⚖️", "LLB programs", "undergrad"),
            Subject("cs", "Computer Science", "💻", "Programming & AI", "undergrad")
        ),
        "postgrad" to listOf(
            Subject("mtech", "M.Tech/MS", "🔧", "Engineering specialization", "postgrad"),
            Subject("mba", "MBA", "📊", "Business management", "postgrad"),
            Subject("msc", "M.Sc", "🧬", "Science research", "postgrad"),
            Subject("ma", "M.A", "📜", "Arts & humanities", "postgrad"),
            Subject("mca", "MCA", "💻", "Computer applications", "postgrad"),
            Subject("md", "MD/MS", "🩺", "Medical specialization", "postgrad")
        ),
        "phd" to listOf(
            Subject("research", "Research Methodology", "📊", "Research design", "phd"),
            Subject("thesis", "Thesis Writing", "📝", "Academic writing", "phd"),
            Subject("publication", "Publications", "📄", "Journal papers", "phd"),
            Subject("teaching", "Teaching Methods", "👨‍🏫", "Pedagogy", "phd"),
            Subject("domain", "Domain Expertise", "🎯", "Specialized knowledge", "phd")
        )
    )
    
    fun selectLevel(level: EducationLevel) {
        _uiState.value = _uiState.value.copy(
            selectedLevel = level,
            subjects = allSubjects[level.id] ?: emptyList()
        )
    }
    
    fun searchSubjects(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun getFilteredSubjects(): List<Subject> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.subjects
        } else {
            _uiState.value.subjects.filter {
                it.name.lowercase().contains(query) || 
                it.description.lowercase().contains(query)
            }
        }
    }
}
