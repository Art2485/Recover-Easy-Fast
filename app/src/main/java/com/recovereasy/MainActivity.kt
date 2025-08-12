package com.recovereasy

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RecoverEasyApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverEasyApp() {
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var audios by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> images = uris ?: emptyList() }

    val pickVideos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> videos = uris ?: emptyList() }

    val pickAudios = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> audios = uris ?: emptyList() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RecoverEasy") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pickImages.launch(arrayOf("image/*")) }) { Text("เลือกรูป") }
                Button(onClick = { pickVideos.launch(arrayOf("video/*")) }) { Text("เลือกวิดีโอ") }
                Button(onClick = { pickAudios.launch(arrayOf("audio/*")) }) { Text("เลือกเสียง") }
            }

            if (images.isEmpty() && videos.isEmpty() && audios.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("RecoverEasy is running 🎉")
                }
            } else {
                Text("รูปภาพ (${images.size})")
                MediaUriList(images)
                Spacer(Modifier.height(8.dp))
                Text("วิดีโอ (${videos.size})")
                MediaUriList(videos)
                Spacer(Modifier.height(8.dp))
                Text("เสียง (${audios.size})")
                MediaUriList(audios, showThumb = false)
            }
        }
    }
}

@Composable
private fun MediaUriList(uris: List<Uri>, showThumb: Boolean = true) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(uris) { uri ->
            Row(
                Modifier.fillMaxWidth().clickable { /* TODO: เปิดเล่น/แชร์ */ }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showThumb) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(uri.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}
