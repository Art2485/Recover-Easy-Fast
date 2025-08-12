package com.recovereasy

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var recycler: RecyclerView
    private val adapter = MediaAdapter()
    private val uiScope = CoroutineScope(Job() + Dispatchers.Main)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it } // ขอสักอันผ่านพอ (ภาพ/วิดีโอ/เสียง)
        if (granted) loadMedia()
        else Toast.makeText(this, R.string.perm_denied, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = adapter

        ensurePermissionAndLoad()
    }

    private fun ensurePermissionAndLoad() {
        val needs = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.READ_MEDIA_VIDEO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.READ_MEDIA_AUDIO
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (needs.isEmpty()) loadMedia() else permissionLauncher.launch(needs.toTypedArray())
    }

    private fun loadMedia() {
        uiScope.launch {
            val data = withContext(Dispatchers.IO) {
                queryAllMedia()
            }
            adapter.submitList(data)
            Toast.makeText(this@MainActivity, getString(R.string.loaded, data.size), Toast.LENGTH_SHORT).show()
        }
    }

    private fun queryAllMedia(): List<MediaItem> {
        val out = ArrayList<MediaItem>()

        // รูปภาพ
        out += queryType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaType.IMAGE,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME
            )
        )

        // วิดีโอ
        out += queryType(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaType.VIDEO,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DISPLAY_NAME
            )
        )

        // เสียง
        out += queryType(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaType.AUDIO,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME
            )
        )

        // เรียงใหม่ตามวันที่ล่าสุดก่อน
        return out.sortedByDescending { it.dateAdded }
    }

    private fun queryType(
        uri: android.net.Uri,
        type: MediaType,
        projection: Array<String>
    ): List<MediaItem> {
        val list = ArrayList<MediaItem>()
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { c ->
            val idIdx = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeIdx = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeIdx = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateIdx = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val nameIdx = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            while (c.moveToNext()) {
                val id = c.getLong(idIdx)
                val contentUri = ContentUris.withAppendedId(uri, id)
                list += MediaItem(
                    id = id,
                    uri = contentUri,
                    mime = c.getString(mimeIdx) ?: "",
                    size = c.getLong(sizeIdx),
                    dateAdded = c.getLong(dateIdx),
                    type = type,
                    displayName = c.getString(nameIdx) ?: ""
                )
            }
        }
        return list
    }
}
