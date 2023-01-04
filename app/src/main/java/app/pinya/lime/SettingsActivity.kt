package app.pinya.lime

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : Activity() {
    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        state = State(this)
        state.fetchInstalledAppsAgain()

        initializeBackButton()

        initializeDateSeekBar()
        initializeTimeSeekBar()

        initializeDailyWallpaperText()
        initializeDimBackgroundText()
        initializeUseBlackText()
        initializeShowHiddenApps()
        initializeAutoOpenKeyboard()
        initializeAutoOpenApps()
        initializeShowIconsInHome()
        initializeShowIconsInDrawer()
        initializeShowSearchBarInDrawer()
        initializeShowAlphabetFilterInDrawer()
        initializeTimeAppLaunch()
        initializeDateAppLaunch()
    }


    override fun onResume() {
        super.onResume()

        state.fetchInstalledAppsAgain()
        state.hideMenu()
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

                state.vibrate()
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

                state.vibrate()
                state.saveData(DataKey.TIME_FORMAT, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeDailyWallpaperText() {
        val switch = findViewById<Switch>(R.id.dailyWallpaperSwitch)
        val stateValue = state.getData(DataKey.DAILY_WALLPAPER, false)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.DAILY_WALLPAPER, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeDimBackgroundText() {
        val switch = findViewById<Switch>(R.id.dimBackgroundSwitch)
        val stateValue = state.getData(DataKey.DIM_BACKGROUND, true)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.DIM_BACKGROUND, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeUseBlackText() {
        val switch = findViewById<Switch>(R.id.blackTextSwitch)
        val stateValue = state.getData(DataKey.BLACK_TEXT, false)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.BLACK_TEXT, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowHiddenApps() {
        val switch = findViewById<Switch>(R.id.showHiddenAppsSwitch)
        val stateValue = state.getData(DataKey.SHOW_HIDDEN_APPS, false)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.SHOW_HIDDEN_APPS, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeAutoOpenKeyboard() {
        val autoKeyboardSwitch = findViewById<Switch>(R.id.autoKeyboardSwitch)
        val stateValue = state.getData(DataKey.AUTO_SHOW_KEYBOARD, true)

        autoKeyboardSwitch.isChecked = stateValue

        autoKeyboardSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.AUTO_SHOW_KEYBOARD, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeAutoOpenApps() {
        val autoOpenAppsSwitch = findViewById<Switch>(R.id.autoOpenAppsSwitch)
        val stateValue = state.getData(DataKey.AUTO_OPEN_APPS, true)

        autoOpenAppsSwitch.isChecked = stateValue

        autoOpenAppsSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.AUTO_OPEN_APPS, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowIconsInHome() {
        val iconsInHomeSwitch = findViewById<Switch>(R.id.iconsInHomeSwitch)
        val stateValue = state.getData(DataKey.ICONS_IN_HOME, true)

        iconsInHomeSwitch.isChecked = stateValue

        iconsInHomeSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.ICONS_IN_HOME, isChecked)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowIconsInDrawer() {
        val iconsInDrawerSwitch = findViewById<Switch>(R.id.iconsInDrawerSwitch)
        val stateValue = state.getData(DataKey.ICONS_IN_DRAWER, true)

        iconsInDrawerSwitch.isChecked = stateValue

        iconsInDrawerSwitch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.ICONS_IN_DRAWER, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowSearchBarInDrawer() {
        val switch = findViewById<Switch>(R.id.showSearchBarSwitch)
        val stateValue = state.getData(DataKey.SHOW_SEARCH_BAR, true)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.SHOW_SEARCH_BAR, isChecked)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initializeShowAlphabetFilterInDrawer() {
        val switch = findViewById<Switch>(R.id.showAlphabetSwitch)
        val stateValue = state.getData(DataKey.SHOW_ALPHABET_FILTER, true)

        switch.isChecked = stateValue

        switch.setOnCheckedChangeListener { _, isChecked ->
            state.vibrate()
            state.saveData(DataKey.SHOW_ALPHABET_FILTER, isChecked)
        }
    }


    private fun onTimeChangeApp(
        useDefault: Boolean, useNone: Boolean, newAppPackage: String
    ) {
        if (useDefault) {
            state.saveData(DataKey.TIME_CLICK_APP, "default")
            updateTimeLaunchApp()
            return
        }

        if (useNone) {
            state.saveData(DataKey.TIME_CLICK_APP, "none")
            updateTimeLaunchApp()
            return
        }

        state.saveData(DataKey.TIME_CLICK_APP, newAppPackage)
        updateTimeLaunchApp()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeTimeAppLaunch() {
        val button = findViewById<TextView>(R.id.timeAppLaunchButton)
        val container = findViewById<ConstraintLayout>(R.id.contextMenuSettings_parent)
        val appLayout = findViewById<LinearLayout>(R.id.timeAppLaunchApp)

        updateTimeLaunchApp()

        button.setOnClickListener {
            state.vibrate()
            state.showAppListMenu(true, container, ::onTimeChangeApp)
        }

        appLayout.setOnClickListener {
            state.vibrate()
            state.showAppListMenu(true, container, ::onTimeChangeApp)
        }
    }


    private fun updateTimeLaunchApp() {
        val stateValue = state.getData(DataKey.TIME_CLICK_APP, "default")
        val timeAppIcon = findViewById<ImageView>(R.id.timeAppIcon)
        val timeAppName = findViewById<TextView>(R.id.timeAppName)

        when (stateValue) {
            "default" -> {
                timeAppIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.icon_clock
                    )
                )
                timeAppName.text = "Default clock app"
            }
            "none" -> {
                timeAppIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_close))
                timeAppName.text = "Don't open any app"
            }
            else -> {
                val app =
                    state.getInstalledAppList().find { it.packageName == stateValue } ?: return
                timeAppIcon.setImageDrawable(app.icon)
                timeAppName.text = app.name
            }
        }
    }


    private fun onDateChangeApp(
        useDefault: Boolean, useNone: Boolean, newAppPackage: String
    ) {
        if (useDefault) {
            state.saveData(DataKey.DATE_CLICK_APP, "default")
            updateDateLaunchApp()
            return
        }

        if (useNone) {
            state.saveData(DataKey.DATE_CLICK_APP, "none")
            updateDateLaunchApp()
            return
        }

        state.saveData(DataKey.DATE_CLICK_APP, newAppPackage)
        updateDateLaunchApp()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeDateAppLaunch() {
        val button = findViewById<TextView>(R.id.dateAppLaunchButton)
        val container = findViewById<ConstraintLayout>(R.id.contextMenuSettings_parent)
        val appLayout = findViewById<LinearLayout>(R.id.dateAppLaunchApp)

        updateDateLaunchApp()

        button.setOnClickListener {
            state.vibrate()
            state.showAppListMenu(false, container, ::onDateChangeApp)
        }

        appLayout.setOnClickListener {
            state.vibrate()
            state.showAppListMenu(false, container, ::onDateChangeApp)
        }
    }

    private fun updateDateLaunchApp() {
        val stateValue = state.getData(DataKey.DATE_CLICK_APP, "default")
        val dateAppIcon = findViewById<ImageView>(R.id.dateAppIcon)
        val dateAppName = findViewById<TextView>(R.id.dateAppName)

        when (stateValue) {
            "default" -> {
                dateAppIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.icon_calendar
                    )
                )
                dateAppName.text = "Default calendar app"
            }
            "none" -> {
                dateAppIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_close))
                dateAppName.text = "Don't open any app"
            }
            else -> {
                val app =
                    state.getInstalledAppList().find { it.packageName == stateValue } ?: return
                dateAppIcon.setImageDrawable(app.icon)
                dateAppName.text = app.name
            }
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