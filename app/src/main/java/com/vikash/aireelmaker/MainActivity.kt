package com.vikash.aireelmaker

import android.os.Bundle
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.android.billingclient.api.*

class MainActivity : ComponentActivity() {
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}

        billingClient = BillingClient.newBuilder(this)
            .setListener { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    Toast.makeText(this, "Purchase received. Verify on your backend before granting premium.", Toast.LENGTH_LONG).show()
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {}
            override fun onBillingServiceDisconnected() {}
        })

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF8AB4F8),
                    background = Color(0xFF101114),
                    surface = Color(0xFF181A1F)
                )
            ) {
                ReelMakerScreen()
            }
        }
    }

    fun buyPremium() {
        thread {
            try {
                val url = URL("https://potential-space-adventure-p7wq4vjxx7qq3949x-8080.app.github.dev/v1/subscribe")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.outputStream.use {
                    it.write("""{"userId":"vikash-user"}""".toByteArray())
                }

                val success = conn.responseCode in 200..299
                conn.disconnect()

                Handler(Looper.getMainLooper()).post {
                    if (success) {
                        getSharedPreferences("reel_prefs", 0)
                            .edit()
                            .putBoolean("premium", true)
                            .apply()
                        Toast.makeText(this, "Premium activated", Toast.LENGTH_LONG).show()
                        recreate()
                    } else {
                        Toast.makeText(this, "Subscription failed", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Server connection failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun ReelMakerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("reel_prefs", 0) }
    var usedFreeReel by remember { mutableStateOf(false) }
    var premium by remember { mutableStateOf(prefs.getBoolean("premium", false)) }
    var prompt by remember { mutableStateOf("") }
    var generating by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf(4) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var reelHistory by remember { mutableStateOf(prefs.getStringSet("reel_history", emptySet())?.toList() ?: emptyList()) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!premium) {
                AndroidBannerAd()
            }
        },
        containerColor = Color(0xFF101114)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSettings) {
                SettingsScreen(
                    premium = premium,
                    onBack = { showSettings = false },
                    onPremium = { (context as? MainActivity)?.buyPremium() }
                )
            } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B1D22)
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "VIKASH MEHTA",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "AI REELS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8AB4F8)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Create • Generate • Share",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                    }

                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Reels",
                        modifier = Modifier.size(42.dp),
                        tint = Color(0xFF8AB4F8)
                    )
                }
            }

            OutlinedButton(
                onClick = { showSettings = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("⚙ Settings & My Account")
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (premium)
                        Color(0xFF20352A)
                    else
                        Color(0xFF252830)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (premium) Icons.Default.WorkspacePremium
                        else Icons.Default.Lock,
                        contentDescription = "Plan"
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (premium) "PREMIUM ACTIVE" else "FREE PLAN",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                "Create cinematic AI reels with VIKASH MEHTA 8051.",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Text(
                "Reel Templates",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            val templates = listOf(
                "🔥 Motivational" to "A cinematic motivational reel about success, hard work and never giving up.",
                "❤️ Emotional" to "An emotional cinematic reel about memories, feelings and life.",
                "😎 Attitude" to "A powerful cinematic attitude reel with confident visuals and dramatic camera shots.",
                "🎬 Cinematic" to "A high-quality cinematic reel with dramatic lighting, smooth camera movement and epic visuals.",
                "🎵 Trending" to "A modern energetic social media reel with fast cuts, dynamic visuals and trending style.",
                "🇮🇳 Patriotic" to "A cinematic patriotic reel celebrating India with inspiring visuals and powerful emotions."
            )

            templates.chunked(2).forEach { rowTemplates ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowTemplates.forEach { (title, templatePrompt) ->
                        OutlinedButton(
                            onClick = { prompt = templatePrompt },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(title, fontSize = 12.sp)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D22)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Your Reel", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Example: cinematic motivational reel about success") }
                    )

                    Text(
                        "Duration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 8).forEach { duration ->
                            if (duration == selectedDuration) {
                                Button(
                                    onClick = { selectedDuration = duration },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${duration}s")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedDuration = duration },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${duration}s")
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            generating = true
                            thread {
                                try {
                                    val url = URL("https://potential-space-adventure-p7wq4vjxx7qq3949x-8080.app.github.dev/v1/reels/generate")
                                    val conn = url.openConnection() as HttpURLConnection
                                    conn.requestMethod = "POST"
                                    conn.setRequestProperty("Content-Type", "application/json")
                                    conn.doOutput = true

                                    val body = """{"userId":"vikash-user","prompt":"${prompt.replace("\"", "'")}","duration":$selectedDuration}"""
                                    conn.outputStream.use { it.write(body.toByteArray()) }

                                    val code = conn.responseCode
                                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                                    val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
                                    conn.disconnect()

                                    Handler(Looper.getMainLooper()).post {
                                        generating = false

                                        if (code == 402 || response.contains("FREE REEL USED")) {
                                            usedFreeReel = true
                                            prefs.edit().putBoolean("used_free_reel", true).apply()
                                            Toast.makeText(
                                                context,
                                                "Your free Reel is already used. Please subscribe.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else if (code in 200..299) {
                                            val match = Regex(""""videoUrl":"([^"]+)"""").find(response)
                                            videoUrl = match?.groupValues?.get(1)

                                            videoUrl?.let { savedUrl ->
                                                val dateTime = java.text.SimpleDateFormat(
                                                    "dd MMM yyyy, hh:mm a",
                                                    java.util.Locale.getDefault()
                                                ).format(java.util.Date())

                                                val item = prompt.trim() + "||" + savedUrl + "||" + dateTime
                                                val updatedHistory = (reelHistory + item).takeLast(20)
                                                reelHistory = updatedHistory
                                                prefs.edit().putStringSet("reel_history", updatedHistory.toSet()).apply()
                                            }

                                            usedFreeReel = true
                                            prefs.edit().putBoolean("used_free_reel", true).apply()
                                            Toast.makeText(
                                                context,
                                                "Reel generated successfully!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Server connection failed.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Handler(Looper.getMainLooper()).post {
                                        generating = false
                                        Toast.makeText(
                                            context,
                                            "Server connection failed.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !generating && prompt.isNotBlank()
                    ) {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (generating) "Generating..." else "CREATE REEL")
                    }
                }
            }

            if (reelHistory.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D22)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "My Reels",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        reelHistory.asReversed().forEachIndexed { index, item ->
                            val parts = item.split("||", limit = 2)
                            val title = parts.getOrNull(0) ?: "Generated Reel"

                            Card(
                                onClick = {
                                    val savedUrl = parts.getOrNull(1)
                                    if (!savedUrl.isNullOrBlank()) {
                                        videoUrl = savedUrl
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF252830)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF30343D)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            title.ifBlank { "Generated Reel ${index + 1}" },
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            "Tap to play",
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val updatedHistory = reelHistory.filter { it != item }
                                            reelHistory = updatedHistory
                                            prefs.edit()
                                                .putStringSet("reel_history", updatedHistory.toSet())
                                                .apply()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Reel"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (videoUrl != null) {
                Button(
                    onClick = {
                        thread {
                            try {
                                val fullUrl = "https://potential-space-adventure-p7wq4vjxx7qq3949x-8080.app.github.dev" + videoUrl
                                val connection = URL(fullUrl).openConnection()
                                val input = connection.getInputStream()

                                val fileName = "Vikash_Reel_${System.currentTimeMillis()}.mp4"
                                val values = android.content.ContentValues().apply {
                                    put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, fileName)
                                    put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                                    put(
                                        android.provider.MediaStore.Video.Media.RELATIVE_PATH,
                                        android.os.Environment.DIRECTORY_MOVIES + "/VIKASH MEHTA AI REELS"
                                    )
                                }

                                val resolver = context.contentResolver
                                val uri = resolver.insert(
                                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    values
                                )

                                if (uri != null) {
                                    resolver.openOutputStream(uri)?.use { output ->
                                        input.copyTo(output)
                                    }
                                    input.close()

                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            context,
                                            "Reel saved to Movies/VIKASH MEHTA AI REELS",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    input.close()
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            context,
                                            "Unable to save Reel",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Download failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                    Spacer(Modifier.width(8.dp))
                    Text("DOWNLOAD REEL")
                }

                Button(
                    onClick = {
                        videoUrl?.let { savedUrl ->
                            val shareUrl =
                                "https://potential-space-adventure-p7wq4vjxx7qq3949x-8080.app.github.dev" + savedUrl

                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_SEND
                            ).apply {
                                type = "video/*"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    shareUrl
                                )
                            }

                            context.startActivity(
                                android.content.Intent.createChooser(
                                    intent,
                                    "Share Reel"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(Modifier.width(8.dp))
                    Text("SHARE REEL")
                }

                AndroidView(
                    factory = { context ->
                        android.widget.VideoView(context).apply {
                            setVideoURI(
                                android.net.Uri.parse(
                                    "https://potential-space-adventure-p7wq4vjxx7qq3949x-8080.app.github.dev" + videoUrl
                                )
                            )
                            setOnPreparedListener {
                                it.isLooping = true
                                start()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            if (!premium) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20242B)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lock, null)
                        Text("Free Reel used", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Subscribe to unlock more AI Reel generations and remove ads.")
                        Button(
                            onClick = { (context as? MainActivity)?.buyPremium() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null)
                            Spacer(Modifier.width(8.dp))
                            Text("GET PREMIUM")
                        }
                    }
                }
            }

            if (premium) {
                AssistChip(
                    onClick = {},
                    label = { Text("Premium Active") },
                    leadingIcon = { Icon(Icons.Default.WorkspacePremium, null) }
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A1F)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Premium plan", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Example: ₹99/month")
                    Text("• More Reel generations")
                    Text("• Premium templates")
                    Text("• No banner ads")
                    Text("• Faster generation")
                }
            TextButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://potential-space-adventure-p7wq4vjxx7qq3949x-8000.app.github.dev/public/privacy_policy.html"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }

            }
        }
    }
    }
}

@Composable
fun AndroidBannerAd() {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // Google test banner ID. Replace with your AdMob unit ID before release.
                adUnitId = "ca-app-pub-3940256099942544/9214589741"
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF101114))
    )
}

@Composable
fun SettingsScreen(
    premium: Boolean,
    onBack: () -> Unit,
    onPremium: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to AI Reels")
        }

        Text(
            "Settings & My Account",
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B1D22)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "👤 VIKASH MEHTA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "AI Reels Creator",
                    fontSize = 14.sp
                )
                Text("User ID: vikash-user")
                Text(
                    if (premium) "⭐ Plan: Premium Active"
                    else "🆓 Plan: Free"
                )
                Text(
                    "App Version: 1.0",
                    fontSize = 13.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (premium)
                    Color(0xFF20352A)
                else
                    Color(0xFF181A1F)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = "Premium"
                )

                Text(
                    if (premium) "Premium Active" else "Upgrade to Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (premium)
                        "You have access to premium features."
                    else
                        "Unlock more AI Reel generations and remove banner ads."
                )

                if (!premium) {
                    Button(
                        onClick = onPremium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GET PREMIUM")
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF181A1F)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "App Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("• AI Reel creation")
                Text("• Reel history")
                Text("• Download & Share")
                Text("• Privacy Policy")
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF181A1F)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "ℹ About App",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("VIKASH MEHTA AI REELS")
                Text("Create AI-powered reels quickly and easily.")
                Text("Version: 1.0")
                Text("Creator: VIKASH MEHTA")
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF181A1F)
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🆘 Help & Support",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("• Check your internet connection")
                Text("• Make sure the AI Reel prompt is not empty")
                Text("• Try generating again if a request fails")
                Text("• For app issues, contact the app support team")
            }
        }
    }
}
