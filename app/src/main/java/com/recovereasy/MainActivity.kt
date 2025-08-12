package com.recovereasy

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Tab { Images, Videos, Audio }

data class MediaEntry(
    val id: Long,
    val uri: Uri,
    val name: String,
    val mime: String,
    val size: Long,
    val durationMs: Long? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RecoverEasyApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecoverEasyApp() {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(Tab.Images) }
    var hasPerm by remember { mutableStateOf(false) }
    var isRequesting by remember { mutableStateOf(false) }

    // launcher สำหรับขอ permission ตามเวอร์ชั่น
    val perms = remember {
        if (Build.VERSION.SDK_INT >= 33) arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        ) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasPerm = res.values.any { it } ||
                perms.all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }
        isRequesting = false
    }

    // เช็ค permission ครั้งแรก
    LaunchedEffect(Unit) {
        hasPerm = perms.all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }
        if (!hasPerm && !isRequesting) {
            isRequesting = true
            launcher.launch(perms)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RecoverEasy") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Images,
                    onClick = { tab = Tab.Images },
                    icon = { Icon(Icons.Outlined.Image, null) },
                    label = { Text("Images") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Videos,
                    onClick = { tab = Tab.Videos },
                    icon = { Icon(Icons.Outlined.Movie, null) },
                    label = { Text("Videos") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Audio,
                    onClick = { tab = Tab.Audio },
                    icon = { Icon(Icons.Outlined.AudioFile, null) },
                    label = { Text("Audio") }
                )
            }
        }
    ) { padding ->
        if (!hasPerm) {
            PermissionRequest(
                modifier = Modifier.padding(padding),
                onRequest = { launcher.launch(perms) }
            )
            return@Scaffold
        }

        when (tab) {
            Tab.Images -> MediaGrid(
                modifier = Modifier.padding(padding),
                loader = { queryImages(ctx) }
            )
            Tab.Videos -> MediaGrid(
                modifier = Modifier.padding(padding),
                loader = { queryVideos(ctx) }
            )
            Tab.Audio -> AudioList(
                modifier = Modifier.padding(padding),
                loader = { queryAudio(ctx) }
            )
        }
    }
}

@Composable
fun PermissionRequest(modifier: Modifier = Modifier, onRequest: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ต้องการสิทธิ์เพื่ออ่านสื่อในเครื่อง", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("กดอนุญาตเพื่อให้แอพแสดงรูป วิดีโอ และไฟล์เสียงของคุณ")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text("ขออนุญาต") }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    modifier: Modifier = Modifier,
    loader: suspend () -> List<MediaEntry>
) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf<List<MediaEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(loader) {
        loading = true
        items = loader()
        loading = false
    }

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ไม่พบสื่อในอุปกรณ์")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier.fillMaxSize().padding(8.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items, key = { it.id }) { media ->
            AsyncImage(
                model = media.uri,
                contentDescription = media.name,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            openUri(ctx, media.uri, media.mime)
                        },
                        onLongClick = {
                            shareUri(ctx, media.uri, media.mime)
                        }
                    )
            )
        }
    }
}

@Composable
fun AudioList(
    modifier: Modifier = Modifier,
    loader: suspend () -> List<MediaEntry>
) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf<List<MediaEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(loader) {
        loading = true
        items = loader()
        loading = false
    }

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { media ->
            ListItem(
                headlineContent = {
                    Text(media.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(media.mime)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { openUri(ctx, media.uri, media.mime) },
                        onLongClick = { shareUri(ctx, media.uri, media.mime) }
                    )
            )
            Divider()
        }
    }
}

/* ---------- Utils: query & intents ---------- */

private suspend fun queryImages(ctx: android.content.Context): List<MediaEntry> =
    withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaEntry>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE
        )
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ctx.contentResolver.query(uri, projection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                list += MediaEntry(id, contentUri, c.getString(nameCol), c.getString(mimeCol), c.getLong(sizeCol))
            }
        }
        list
    }

private suspend fun queryVideos(ctx: android.content.Context): List<MediaEntry> =
    withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaEntry>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ctx.contentResolver.query(uri, projection, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                list += MediaEntry(id, contentUri, c.getString(nameCol), c.getString(mimeCol), c.getLong(sizeCol), c.getLong(durCol))
            }
        }
        list
    }

private suspend fun queryAudio(ctx: android.content.Context): List<MediaEntry> =
    withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaEntry>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        ctx.contentResolver.query(uri, projection, null, null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                list += MediaEntry(id, contentUri, c.getString(nameCol), c.getString(mimeCol), c.getLong(sizeCol))
            }
        }
        list
    }

private fun openUri(ctx: android.content.Context, uri: Uri, mime: String) {
    val i = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(i)
}

private fun shareUri(ctx: android.content.Context, uri: Uri, mime: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(i, "แชร์ด้วย..."))
}
