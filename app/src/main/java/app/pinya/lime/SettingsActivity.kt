package app.pinya.lime

import android.annotation.SuppressLint
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

        initializeUseBlackText()
        initializeShowHiddenApps()
        initializeAutoOpenKeyboard()
        initializeAutoOpenApps()
        initializeShowIconsInHome()
        initializeShowIconsInDrawer()
        initializeShowSearchBarInDrawer()
        initializeShowAlphabetFilterInDrawer()
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
                if (progress < 0 || progress > 2) return
                timeExample.text =
                    SimpleDateFormat.getTimeInstance(mapFormat(progress)).format(Date())

                state.saveData(DataKey.TIME_FORMAT, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeUseBlackText() {
        val switch = findViewById<Switch>(R.id.blackTextSwitch)
        val stateValue = state.getData(DataKey.BLACK_TEXT, false)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.BLACK_TEXT, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowHiddenApps() {
        val switch = findViewById<Switch>(R.id.showHiddenAppsSwitch)
        val stateValue = state.getData(DataKey.SHOW_HIDDEN_APPS, false)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.SHOW_HIDDEN_APPS, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeAutoOpenKeyboard() {
        val autoKeyboardSwitch = findViewById<Switch>(R.id.autoKeyboardSwitch)
        val stateValue = state.getData(DataKey.AUTO_SHOW_KEYBOARD, true)

        autoKeyboardSwitch.isChecked = stateValue

        autoKeyboardSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.AUTO_SHOW_KEYBOARD, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeAutoOpenApps() {
        val autoOpenAppsSwitch = findViewById<Switch>(R.id.autoOpenAppsSwitch)
        val stateValue = state.getData(DataKey.AUTO_OPEN_APPS, true)

        autoOpenAppsSwitch.isChecked = stateValue

        autoOpenAppsSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.AUTO_OPEN_APPS, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowIconsInHome() {
        val iconsInHomeSwitch = findViewById<Switch>(R.id.iconsInHomeSwitch)
        val stateValue = state.getData(DataKey.ICONS_IN_HOME, true)

        iconsInHomeSwitch.isChecked = stateValue

        iconsInHomeSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.ICONS_IN_HOME, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowIconsInDrawer() {
        val iconsInDrawerSwitch = findViewById<Switch>(R.id.iconsInDrawerSwitch)
        val stateValue = state.getData(DataKey.ICONS_IN_DRAWER, true)

        iconsInDrawerSwitch.isChecked = stateValue

        iconsInDrawerSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.ICONS_IN_DRAWER, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowSearchBarInDrawer() {
        val switch = findViewById<Switch>(R.id.showSearchBarSwitch)
        val stateValue = state.getData(DataKey.SHOW_SEARCH_BAR, true)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.SHOW_SEARCH_BAR, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowAlphabetFilterInDrawer() {
        val switch = findViewById<Switch>(R.id.showAlphabetSwitch)
        val stateValue = state.getData(DataKey.SHOW_ALPHABET_FILTER, true)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.saveData(DataKey.SHOW_ALPHABET_FILTER, isChecked)
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