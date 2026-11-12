package com.centralhex.hexai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val Black = Color(0xFF080808)
private val Panel = Color(0xFF171717)
private val White = Color(0xFFF5F5F5)
private val Muted = Color(0xFFAAAAAA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HexAiApp(this) }
    }
}

data class ChatMessage(
    val text: String,
    val fromUser: Boolean
)

@Composable
fun HexAiApp(context: Context) {
    val prefs = remember {
        context.getSharedPreferences("hex_ai", Context.MODE_PRIVATE)
    }

    var serverUrl by remember {
        mutableStateOf(
            prefs.getString("server_url", "") ?: ""
        )
    }

    var prompt by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Black,
            surface = Panel,
            primary = White,
            onPrimary = Black,
            onBackground = White,
            onSurface = White
        )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "CENTRAL-HEX",
                        color = Muted,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        "HEX-AI",
                        color = White,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Ton assistant intelligent",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showSettings = true }
                ) {
                    Text("⚙ Réglages")
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Assistant", "Code", "Idées", "Traduction")
                    .forEach { mode ->
                        AssistChip(
                            onClick = {
                                prompt = when (mode) {
                                    "Code" ->
                                        "Aide-moi à écrire ou expliquer du code : "
                                    "Idées" ->
                                        "Propose-moi des idées de projets numériques : "
                                    "Traduction" ->
                                        "Traduis ce texte : "
                                    else -> ""
                                }
                            },
                            label = {
                                Text(mode, color = White)
                            }
                        )
                    }
            }

            Spacer(Modifier.height(10.dp))

            if (messages.isEmpty()) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Panel,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Bienvenue sur HEX-AI ✨",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Pose une question, crée du code, développe une idée ou demande une traduction.",
                        color = Muted
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "L'intelligence au service de ta créativité.",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Panel,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment =
                                if (msg.fromUser) Alignment.End
                                else Alignment.Start
                        ) {
                            Text(
                                if (msg.fromUser) "TOI" else "HEX-AI",
                                color = Muted,
                                fontSize = 10.sp
                            )

                            Text(
                                msg.text,
                                color = White,
                                modifier = Modifier
                                    .widthIn(max = 310.dp)
                                    .background(
                                        if (msg.fromUser)
                                            Color(0xFF303030)
                                        else Color(0xFF202020),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                            )
                        }
                    }

                    if (busy) {
                        item {
                            Text(
                                "HEX-AI réfléchit…",
                                color = Muted
                            )
                        }
                    }
                }
            }

            if (error.isNotBlank()) {
                Text(
                    error,
                    color = Color(0xFFFF7777),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Panel,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = White,
                        fontSize = 15.sp
                    ),
                    decorationBox = { inner ->
                        if (prompt.isEmpty()) {
                            Text(
                                "Écris ton message…",
                                color = Muted
                            )
                        }
                        inner()
                    }
                )

                Button(
                    enabled = !busy && prompt.isNotBlank(),
                    onClick = {
                        val question = prompt.trim()
                        prompt = ""

                        val history = messages.toList()
                        messages.add(ChatMessage(question, true))
                        error = ""
                        busy = true

                        scope.launch {
                            try {
                                val answer = withContext(Dispatchers.IO) {
                                    HexAiClient.ask(
                                        serverUrl,
                                        history,
                                        question
                                    )
                                }

                                messages.add(
                                    ChatMessage(answer, false)
                                )
                            } catch (e: Exception) {
                                error = e.message
                                    ?: "Une erreur est survenue."

                                messages.add(
                                    ChatMessage(
                                        "Je n'ai pas pu obtenir de réponse. Vérifie le serveur et ta connexion.",
                                        false
                                    )
                                )
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text("Envoyer")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "CENTRAL-HEX • L’innovation commence par une idée.",
                color = Muted,
                fontSize = 10.sp
            )
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Connexion HEX-AI") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            singleLine = true,
                            label = { Text("URL du serveur HTTPS") },
                            placeholder = {
                                Text("https://ton-serveur.example.com")
                            }
                        )

                        Text(
                            "Entre l'adresse HTTPS de ton backend Node.js. Ta clé API reste sur le serveur.",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.edit()
                                .putString("server_url", serverUrl.trim())
                                .apply()

                            showSettings = false
                        }
                    ) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSettings = false }
                    ) {
                        Text("Fermer")
                    }
                }
            )
        }
    }
}

private object HexAiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    fun ask(
        baseUrl: String,
        history: List<ChatMessage>,
        question: String
    ): String {
        require(baseUrl.startsWith("https://")) {
            "Configure une URL HTTPS dans Réglages."
        }

        val messages = JSONArray()

        messages.put(
            JSONObject()
                .put("role", "system")
                .put(
                    "content",
                    "Tu es HEX-AI, assistant intelligent de CENTRAL-HEX. Réponds en français sauf demande contraire."
                )
        )

        history.takeLast(12).forEach { message ->
            messages.put(
                JSONObject()
                    .put(
                        "role",
                        if (message.fromUser) "user" else "assistant"
                    )
                    .put("content", message.text)
            )
        }

        messages.put(
            JSONObject()
                .put("role", "user")
                .put("content", question)
        )

        val body = JSONObject()
            .put("messages", messages)
            .toString()
            .toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )

        val url = baseUrl.trimEnd('/') + "/api/chat"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Erreur serveur ${response.code}: ${raw.take(300)}"
                )
            }

            return JSONObject(raw).getString("reply")
        }
    }
}
