package app.pinya.lime

import android.app.Activity
import android.os.Bundle

class SettingsActivity : Activity() {
    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        state = State(this)
    }
}