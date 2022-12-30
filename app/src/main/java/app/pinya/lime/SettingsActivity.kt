package app.pinya.lime

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton

class SettingsActivity : Activity() {
    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        state = State(this)

        initializeButtons()
    }

    private fun initializeButtons() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }
} 