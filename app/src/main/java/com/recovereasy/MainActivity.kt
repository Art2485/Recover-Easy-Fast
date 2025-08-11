package com.recovereasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.setContentView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = MediaAdapter()
        rv.adapter = adapter

        // ตัวอย่างข้อมูล (ภาพจาก picsum)
        adapter.submitList(
            List(20) { i ->
                MediaItem(
                    title = "Item #$i",
                    subtitle = "Subtitle $i",
                    imageUrl = "https://picsum.photos/seed/$i/200/200"
                )
            }
        )
    }
}
