package com.recovereasy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = MediaAdapter()
        rv.adapter = adapter

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
