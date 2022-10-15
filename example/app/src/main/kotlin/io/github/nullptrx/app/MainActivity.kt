package io.github.nullptrx.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val view = findViewById<TextView>(R.id.text)
    view.text = "example"
    view.setOnClickListener {
      view.text = "example"
    }
  }
}