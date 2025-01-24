package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all icons from filled
import androidx.compose.runtime.*

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.MyApplicationTheme



data class Note(val id: Int, val title: String, val content: String)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var selectedTheme by remember { mutableStateOf("Device Theme") } // Default to system theme
            val isDarkTheme = when (selectedTheme) { // When theme is changed
                "Dark Theme" -> true
                "White Theme" -> false // Light theme
                "Amoled" -> true
                else -> isSystemInDarkTheme() // else if Device theme is selected, check the device system

            }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                var isLoggedIn by remember { mutableStateOf(false) }
                if (!isLoggedIn) {
                    LoginScreen(onLogin = { isLoggedIn = true },onGuest = {isLoggedIn = true} )
                    return@MyApplicationTheme
                }

                MyNoteApp(selectedTheme = selectedTheme, onThemeSelected = { selectedTheme = it })
            }
       }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onCloseSettings:() -> Unit,
    onBackPressed: () -> Unit,
    selectedTheme: String, // Receive selectedTheme as a parameter
    onThemeSelected: (String) -> Unit, // Receive a lambda to update selectedTheme
    themeOptions: List<String>,
) { var language by remember { mutableStateOf("English") }
    val languageOptions = listOf("English","French","Spanish")
    BackHandler(onBack = {onCloseSettings()})
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "share"
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "contact us"
                        )
                    }


                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(

            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            LazyColumn {
                item {   var expanded by remember { mutableStateOf(false) }
                    var selectedOptionText by remember { mutableStateOf(selectedTheme) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            onValueChange = {},
                            readOnly = true,
                            value = selectedOptionText,
                            label = { Text("Theme") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            themeOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        selectedOptionText = selectionOption
                                        onThemeSelected(selectionOption)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                item {
                    var expanded by remember { mutableStateOf(false) }
                    var selectedOptionText by remember { mutableStateOf(language) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = selectedOptionText,
                            onValueChange = {},
                            label = { Text("Language") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            languageOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        selectedOptionText = selectionOption
                                        language = selectionOption

                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable // Add Dialog for adding new note
fun MyNoteApp(selectedTheme: String, onThemeSelected: (String) -> Unit) {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var noteCounter by remember { mutableStateOf(0) }
    var expandedNoteId by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) } // Show dialog for adding notes
    var showSettingsScreen by remember { mutableStateOf(false) }
    var currentSelectedTheme by remember { mutableStateOf(selectedTheme) }

                   val themeOptions = listOf("Device Theme", "Dark Theme", "White Theme","Amoled")
    val backgroundColor = when (currentSelectedTheme) {
        "Amoled" -> Color.Black // Total black for Amoled
        "Dark Theme" -> MaterialTheme.colorScheme.background // standard dark background
        else -> MaterialTheme.colorScheme.background // use the default background for other themes
    }


    if (showSettingsScreen) {
        SettingsScreen(onCloseSettings = {showSettingsScreen = false},onBackPressed = { showSettingsScreen = false }, selectedTheme = currentSelectedTheme, onThemeSelected = { currentSelectedTheme = it; onThemeSelected(it)}, themeOptions = themeOptions)
        return
    }


    Scaffold (
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "My Notes") },
                actions = {
                    IconButton(onClick = { showSettingsScreen = true }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true } ,containerColor = MaterialTheme.colorScheme.primary){
                Icon(Icons.Filled.Add, "Add")
            }
        },
    ) { paddingValues ->
        val currentPaddingValues = PaddingValues(
            paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            paddingValues.calculateTopPadding(),
            paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            paddingValues.calculateBottomPadding())
            Surface (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(currentPaddingValues),
                color = backgroundColor
            ) {
                Box {
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false }
                        ) {
                            AddNoteDialog(
                                onDismissRequest = { showDialog = false },

                                onConfirmation = { title, content ->
                                    notes = notes + Note(noteCounter++, title, content)
                                    showDialog = false
                                },
                                dialogTitle = "Add New Note"
                            )
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {

                        items(notes, key = {note -> note.id }) { note ->
                            val isExpanded = expandedNoteId == note.id // Check if note is expanded
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clickable {
                                            expandedNoteId = if (isExpanded) null else note.id
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    shape = RoundedCornerShape(10.dp)

                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .animateContentSize()
                                    ) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if(isExpanded){
                                            Text(
                                                modifier = Modifier.padding(8.dp),
                                                text = note.content,
                                                style = MaterialTheme.typography.bodyMedium
                                            )}
                                    }
                                }
                        }
                    }
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(onDismissRequest: () -> Unit, onConfirmation: (String, String) -> Unit, dialogTitle: String) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlertDialog(
        title = {
            Text(text = dialogTitle)

        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmation(title, content)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text("Cancel")
            }
        },
        onDismissRequest = { onDismissRequest() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropdownMenu(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    title: String,
) {

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

@Composable
fun LoginScreen(onLogin: () -> Unit, onGuest: () -> Unit) {


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = when(isSystemInDarkTheme()){
            true -> Color.Black
            false -> MaterialTheme.colorScheme.background

        }

    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                        // Sign in with google

                       onLogin()
                        },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(text = "Sign in with Google")
            }

            Button(
                onClick = { onGuest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(text = "Continue as Guest")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // If user is not connected
            TextButton(
                onClick = {
                    Log.d(TAG, "LoginScreen: ")
                    onGuest()

                },
            ) {
                Text(
                    text = "Skip",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

