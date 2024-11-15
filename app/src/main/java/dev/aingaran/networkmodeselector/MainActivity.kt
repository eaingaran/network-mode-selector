package dev.aingaran.networkmodeselector

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aingaran.networkmodeselector.ui.theme.NetworkModeSelectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            NetworkModeSelectorTheme(darkTheme = isSystemInDarkTheme) {
                // Apply the surface to the entire activity
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WarningDialog()
                }
            }
        }
    }
}

@Composable
fun WarningDialog() {
    val context = LocalContext.current
    var showWarningDialog by remember { mutableStateOf(true) }
    var showDialogAgain by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Check if the dialog should be shown
    LaunchedEffect(Unit) {
        showDialogAgain = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_dialog_again", true)
        if (!showDialogAgain) {
            openRadioInfo(context)
        }
    }

    if (showWarningDialog && showDialogAgain) {
        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
            },
            title = { Text("Warning: Proceed with Caution", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("The following settings page allows you to modify critical network configurations. This can potentially lead to network disconnection or other unexpected behavior.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Make sure you understand the implications before proceeding. If you encounter any issues, restarting your device usually resolves them.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aingaran.dev/products/network-mode-selector"))
                            context.startActivity(intent)
                        }) {
                            Text("Help me understand the configuration")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                isChecked = checked
                            }
                        )
                        Text("Don't show this again")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showWarningDialog = false
                    showDialogAgain = !isChecked
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("show_dialog_again", showDialogAgain)
                        .apply()
                    isLoading = true // Show loading indicator
                    openRadioInfo(context)
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("I understand")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showWarningDialog = false
                    (context as ComponentActivity).finish()
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))) {
                    Text("Take me back")
                }
            }
        )
    }

    // Show a CircularProgressIndicator while loading
    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Opening Network Settings...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

fun openRadioInfo(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
        Intent().apply {
            setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
        }
    } else {
        Intent().apply {
            setClassName("com.android.settings", "com.android.settings.RadioInfo")
        }
    }

    try {
        context.startActivity(intent)
        (context as ComponentActivity).finish()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Unable to open Network Settings. Please check your device settings.", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    NetworkModeSelectorTheme(darkTheme = isSystemInDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WarningDialog()
        }
    }
}