package app.pinya.lime

import android.app.Activity
import android.os.Bundle
import android.widget.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : Activity() {
    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        state = State(this)

        initializeBackButton()
        initializeDateSeekBar()
        initializeTimeSeekBar()
        initializeAutoOpenKeyboard()
        initializeAutoOpenApps()
    }

    private fun initializeBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeDateSeekBar() {
        val dateSeekBar = findViewById<SeekBar>(R.id.dateFormat)
        val dateExample = findViewById<TextView>(R.id.dateExample)

        val stateValue = state.getData(DataKey.DATE_FORMAT, 1)

        dateSeekBar.progress = stateValue
        dateExample.text = SimpleDateFormat.getDateInstance(mapFormat(stateValue)).format(Date())

        dateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int, fromUser: Boolean
            ) {
                if (progress < 0 || progress > 3) return
                dateExample.text =
                    SimpleDateFormat.getDateInstance(mapFormat(progress)).format(Date())

                state.saveData(DataKey.DATE_FORMAT, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initializeTimeSeekBar() {
        val timeSeekBar = findViewById<SeekBar>(R.id.timeFormat)
        val timeExample = findViewById<TextView>(R.id.timeExample)

        val stateValue = state.getData(DataKey.TIME_FORMAT, 0)

        timeSeekBar.progress = stateValue
        timeExample.text = SimpleDateFormat.getTimeInstance(mapFormat(stateValue)).format(Date())

        timeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int, fromUser: Boolean
            ) {
                if (progress < 0 || progress > 3) return
                timeExample.text =
                    SimpleDateFormat.getTimeInstance(mapFormat(progress)).format(Date())

                state.saveData(DataKey.TIME_FORMAT, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initializeAutoOpenKeyboard() {
        val autoKeyboardSwitch = findViewById<Switch>(R.id.autoKeyboardSwitch)
        val stateValue = state.getData(DataKey.AUTO_SHOW_KEYBOARD, true)

        autoKeyboardSwitch.isChecked = stateValue

        autoKeyboardSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.AUTO_SHOW_KEYBOARD, isChecked)
        }
    }


    private fun initializeAutoOpenApps() {
        val autoOpenAppsSwitch = findViewById<Switch>(R.id.autoOpenAppsSwitch)
        val stateValue = state.getData(DataKey.AUTO_OPEN_APPS, true)

        autoOpenAppsSwitch.isChecked = stateValue

        autoOpenAppsSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.AUTO_OPEN_APPS, isChecked)
        }
    }

    companion object {
        fun mapFormat(num: Int): Int {
            return when (num) {
                0 -> DateFormat.SHORT
                1 -> DateFormat.MEDIUM
                2 -> DateFormat.LONG
                else -> DateFormat.FULL
            }
        }
    }
} 