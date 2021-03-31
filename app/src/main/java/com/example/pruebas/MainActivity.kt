package com.example.pruebas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pruebas.customsviews.SwipeButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val swipeButton: SwipeButton = findViewById(R.id.swipeButton)
        swipeButton.setOnClickListener {
            Toast.makeText(this,"Le diste Click",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,OtherActivity::class.java)
            startActivity(intent)

        }

    }
}

