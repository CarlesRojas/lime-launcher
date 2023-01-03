package app.pinya.lime

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.round


enum class DataKey {
    DATE_FORMAT, TIME_FORMAT, AUTO_SHOW_KEYBOARD, AUTO_OPEN_APPS, ICONS_IN_HOME, ICONS_IN_DRAWER, SHOW_SEARCH_BAR, SHOW_ALPHABET_FILTER, HOME_APPS, HIDDEN_APPS, SHOW_HIDDEN_APPS, RENAMED_APPS
}

enum class ContextMenuItem {
    ADD_TO_HOME, REMOVE_FROM_HOME, HIDE_APP, SHOW_APP, APP_INFO, UNINSTALL, MOVE_UP, MOVE_DOWN, RENAME_APP
}

class State(context: Context) {

    private val context: Context
    private val preferencesName = "LimeLauncherPreferences"
    private val sharedPreferences: SharedPreferences

    private var contextMenuWindow: PopupWindow? = null
    private var renameWindow: PopupWindow? = null
    private var renameText: String = ""
    private var installedAppList: MutableList<ItemApp> = mutableListOf()
    private var maxNumOfAppsInHome: Int = 100

    init {
        this.context = context
        this.sharedPreferences =
            context.getSharedPreferences(this.preferencesName, Context.MODE_PRIVATE)
    }

    fun getInstalledAppList(): MutableList<ItemApp> {
        return this.installedAppList
    }

    fun fetchInstalledAppsAgain() {
        installedAppList.clear()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val untreatedAppList = context.packageManager.queryIntentActivities(intent, 0)

        val packagesInfo: List<ApplicationInfo> =
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val hiddenApps: MutableSet<String> = getData(DataKey.HIDDEN_APPS, mutableSetOf())
        val homeApps: MutableSet<String> = getData(DataKey.HOME_APPS, mutableSetOf())
        val renamedApps: MutableMap<String, String> = getData(DataKey.RENAMED_APPS, mutableMapOf())

        val showHiddenApps: Boolean = getData(DataKey.SHOW_HIDDEN_APPS, false)

        for (untreatedApp in untreatedAppList) {
            val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
            val packageName = untreatedApp.activityInfo.packageName
            val icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
            val app = ItemApp(name, packageName, icon)

            val packageInfo = packagesInfo.find { it.packageName == packageName }

            app.system =
                if (packageInfo != null) (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM else false
            app.hidden = hiddenApps.find { it == packageName } != null
            app.home = homeApps.find { it == packageName } != null
            app.name = renamedApps[packageName] ?: app.originalName

            if (!showHiddenApps && app.hidden) continue
            if (!installedAppList.contains(app)) installedAppList.add(app)
        }

        homeApps.forEachIndexed { i, packageName ->
            val app = installedAppList.find { packageName == it.packageName }
            if (app != null) app.homeOrderIndex = i
        }

        installedAppList.sortBy { it.name.lowercase() }
    }

    private fun <T> MutableSet<T>.swap(index1: Int, index2: Int) {
        val list = this.toMutableList()

        val tmp = list[index1]
        list[index1] = list[index2]
        list[index2] = tmp
        this.clear()

        list.forEach { this.add(it) }
    }

    private fun isHomeFull(): Boolean {
        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf())

        return homeAppSet.size >= maxNumOfAppsInHome
    }

    private fun toggleHomeInApp(packageName: String, homeNewValue: Boolean) {
        val app = installedAppList.find { it.packageName == packageName }
        app?.home = homeNewValue

        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf())

        when (homeNewValue) {
            true -> homeAppSet.add(packageName)
            false -> homeAppSet.remove(packageName)
        }

        homeAppSet.removeAll { appPackage -> installedAppList.find { appPackage == it.packageName } == null }

        updateHomeAppOrder(homeAppSet)
        saveData(DataKey.HOME_APPS, homeAppSet)
    }

    private fun changeHomeAppOrder(packageName: String, moveUp: Boolean) {
        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf())

        var index = -1
        homeAppSet.forEachIndexed { i, elem ->
            if (elem == packageName) index = i
        }

        if ((moveUp && index <= 0) || (!moveUp && index >= homeAppSet.size - 1) || index < 0 || index >= homeAppSet.size) return

        homeAppSet.swap(index, if (moveUp) index - 1 else index + 1)

        updateHomeAppOrder(homeAppSet)
        saveData(DataKey.HOME_APPS, homeAppSet)
    }

    private fun updateHomeAppOrder(homeAppSet: MutableSet<String>) {
        homeAppSet.forEachIndexed { i, packageName ->
            val app = installedAppList.find { packageName == it.packageName }
            if (app != null) app.homeOrderIndex = i
        }
    }

    fun setMaxNumOfApps(maxNumOfAppsInHome: Int) {
        this.maxNumOfAppsInHome = maxNumOfAppsInHome

        val homeAppList = getData(DataKey.HOME_APPS, mutableSetOf())
        val appsToRemoveFromHome = mutableListOf<String>()
        homeAppList.forEachIndexed { i, elem ->
            if (i >= maxNumOfAppsInHome) appsToRemoveFromHome.add(elem)
        }

        appsToRemoveFromHome.forEach {
            toggleHomeInApp(it, false)
        }
    }

    private fun renameApp(packageName: String, newName: String) {
        if (newName.isEmpty()) return

        val app = installedAppList.find { it.packageName == packageName } ?: return
        app.name = newName

        val renamedAppsMap: MutableMap<String, String> =
            getData(DataKey.RENAMED_APPS, mutableMapOf())

        if (newName == app.originalName) renamedAppsMap.remove(packageName)
        else renamedAppsMap[packageName] = newName

        for (key in renamedAppsMap.keys)
            if (installedAppList.find { key == it.packageName } == null)
                renamedAppsMap.remove(key)

        saveData(DataKey.RENAMED_APPS, renamedAppsMap)
        fetchInstalledAppsAgain()
    }

    private fun toggleHiddenApp(packageName: String, hiddenNewValue: Boolean) {
        val app = installedAppList.find { it.packageName == packageName }
        app?.hidden = hiddenNewValue

        val hiddenAppsSet: MutableSet<String> =
            getData(DataKey.HIDDEN_APPS, mutableSetOf())

        when (hiddenNewValue) {
            true -> hiddenAppsSet.add(packageName)
            false -> hiddenAppsSet.remove(packageName)
        }

        hiddenAppsSet.removeAll { appPackage -> installedAppList.find { appPackage == it.packageName } == null }

        saveData(DataKey.HIDDEN_APPS, hiddenAppsSet)
        fetchInstalledAppsAgain()
    }


    fun saveData(key: DataKey, data: String) {
        val editor = this.sharedPreferences.edit()
        editor.putString(key.toString(), data)
        editor.apply()
    }

    fun saveData(key: DataKey, data: Boolean) {
        val editor = this.sharedPreferences.edit()
        editor.putBoolean(key.toString(), data)
        editor.apply()
    }

    fun saveData(key: DataKey, data: Int) {
        val editor = this.sharedPreferences.edit()
        editor.putInt(key.toString(), data)
        editor.apply()
    }

    fun saveData(key: DataKey, data: Float) {
        val editor = this.sharedPreferences.edit()
        editor.putFloat(key.toString(), data)
        editor.apply()
    }

    fun saveData(key: DataKey, data: MutableSet<String>) {
        val editor = this.sharedPreferences.edit()
        val jsonData: String = Gson().toJson(data)
        editor.putString(key.toString(), jsonData)
        editor.apply()
    }

    fun saveData(key: DataKey, data: MutableMap<String, String>) {
        val editor = this.sharedPreferences.edit()
        val jsonData: String = Gson().toJson(data)
        editor.putString(key.toString(), jsonData)
        editor.apply()
    }

    fun getData(key: DataKey, defaultValue: String): String {
        return this.sharedPreferences.getString(key.toString(), defaultValue) ?: defaultValue
    }

    fun getData(key: DataKey, defaultValue: Boolean): Boolean {
        return this.sharedPreferences.getBoolean(key.toString(), defaultValue)
    }

    fun getData(key: DataKey, defaultValue: Int): Int {
        return this.sharedPreferences.getInt(key.toString(), defaultValue)
    }


    fun getData(key: DataKey, defaultValue: Float): Float {
        return this.sharedPreferences.getFloat(key.toString(), defaultValue)
    }

    fun getData(key: DataKey, defaultValue: MutableSet<String>): MutableSet<String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
        val listType = object : TypeToken<MutableSet<String>>() {}.type
        return Gson().fromJson(result, listType)
    }

    fun getData(
        key: DataKey, defaultValue: MutableMap<String, String>
    ): MutableMap<String, String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
        val listType = object : TypeToken<MutableMap<String, String>>() {}.type
        return Gson().fromJson(result, listType)
    }

    private fun clearDataForKey(key: DataKey) {
        val editor = this.sharedPreferences.edit()
        editor.remove(key.toString())
        editor.apply()
    }


    fun showContextMenu(
        app: ItemApp,
        contextMenuContainer: ConstraintLayout,
        fromHome: Boolean,
        onClickCallback: (item: ContextMenuItem) -> Unit = { }
    ) {
        val contextMenuView = View.inflate(context, R.layout.view_context_menu, null)
        val icon = contextMenuView.findViewById<ImageView>(R.id.appIcon)
        val appName = contextMenuView.findViewById<TextView>(R.id.appName)
        val closeButton = contextMenuView.findViewById<ImageButton>(R.id.closeContextMenuButton)
        val settingsButton = contextMenuView.findViewById<ImageButton>(R.id.settingsButton)

        val homeAppSet: MutableSet<String> = getData(DataKey.HOME_APPS, mutableSetOf())

        val moveUpButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_moveUp)
        val moveDownButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_moveDown)
        val addToHomeButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_addToHome)
        val removeFromHomeButton =
            contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_removeFromHome)
        val showAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_showApp)
        val hideAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_hideApp)
        val appInfoButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_appInfo)
        val uninstallButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_uninstall)
        val renameButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_renameApp)

        icon.setImageDrawable(app.icon)
        appName.text = app.name

        moveUpButton.visibility =
            if (!fromHome || app.homeOrderIndex <= 0) View.GONE else View.VISIBLE
        moveDownButton.visibility =
            if (!fromHome || app.homeOrderIndex >= homeAppSet.size - 1) View.GONE else View.VISIBLE
        addToHomeButton.visibility = if (app.home || isHomeFull()) View.GONE else View.VISIBLE
        removeFromHomeButton.visibility = if (app.home) View.VISIBLE else View.GONE
        showAppButton.visibility = if (app.hidden) View.VISIBLE else View.GONE
        hideAppButton.visibility = if (app.hidden) View.GONE else View.VISIBLE
        uninstallButton.visibility = if (app.system) View.GONE else View.VISIBLE

        contextMenuWindow = PopupWindow(
            contextMenuView,
            contextMenuContainer.width - contextMenuContainer.paddingRight - contextMenuContainer.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.animationStyle = R.style.BottomPopupWindowAnimation

        contextMenuWindow?.showAtLocation(contextMenuContainer, Gravity.BOTTOM, 0, 0)
        dimBehindMenu(contextMenuWindow)

        closeButton.setOnClickListener {
            hideMenu(contextMenuWindow)
        }

        settingsButton.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }

        moveUpButton.setOnClickListener {
            changeHomeAppOrder(app.packageName, true)
            onClickCallback(ContextMenuItem.MOVE_UP)
            hideMenu(contextMenuWindow)
        }

        moveDownButton.setOnClickListener {
            changeHomeAppOrder(app.packageName, false)
            onClickCallback(ContextMenuItem.MOVE_DOWN)
            hideMenu(contextMenuWindow)
        }

        addToHomeButton.setOnClickListener {
            toggleHomeInApp(app.packageName, true)
            onClickCallback(ContextMenuItem.ADD_TO_HOME)
            hideMenu(contextMenuWindow)
        }

        removeFromHomeButton.setOnClickListener {
            toggleHomeInApp(app.packageName, false)
            onClickCallback(ContextMenuItem.REMOVE_FROM_HOME)
            hideMenu(contextMenuWindow)
        }

        showAppButton.setOnClickListener {
            toggleHiddenApp(app.packageName, false)
            onClickCallback(ContextMenuItem.SHOW_APP)
            hideMenu(contextMenuWindow)
        }

        hideAppButton.setOnClickListener {
            toggleHiddenApp(app.packageName, true)
            onClickCallback(ContextMenuItem.HIDE_APP)
            hideMenu(contextMenuWindow)
        }

        appInfoButton.setOnClickListener {
            onClickCallback(ContextMenuItem.APP_INFO)
            hideMenu(contextMenuWindow)

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + app.packageName)
            context.startActivity(intent)
        }

        uninstallButton.setOnClickListener {
            onClickCallback(ContextMenuItem.UNINSTALL)
            hideMenu()

            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:" + app.packageName)
            context.startActivity(intent)
        }

        renameButton.setOnClickListener {
            hideMenu(contextMenuWindow)
            showRenameMenu(app, contextMenuContainer, onClickCallback)
        }
    }

    fun View.focusAndShowKeyboard() {
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun showRenameMenu(
        app: ItemApp,
        renameContainer: ConstraintLayout,
        onRenameCallback: (item: ContextMenuItem) -> Unit = { }
    ) {
        val renameView = View.inflate(context, R.layout.view_rename_menu, null)
        val icon = renameView.findViewById<ImageView>(R.id.appIcon)
        val appName = renameView.findViewById<TextView>(R.id.appName)
        val closeButton = renameView.findViewById<ImageButton>(R.id.closeRenameMenuButton)
        val recoverOriginalNameButton =
            renameView.findViewById<LinearLayout>(R.id.renameMenu_recoverOriginalName)

        val renameBar = renameView.findViewById<EditText>(R.id.renameBar)
        renameText = ""
        renameBar.setText("")

        recoverOriginalNameButton.visibility =
            if (app.name != app.originalName) View.VISIBLE else View.GONE

        fun blurAndHideKeyboard() {
            renameBar.clearFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(renameBar.windowToken, 0)
        }

        icon.setImageDrawable(app.icon)
        appName.text = app.name

        renameWindow = PopupWindow(
            renameView,
            renameContainer.width - renameContainer.paddingRight - renameContainer.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        renameWindow?.animationStyle = R.style.TopPopupWindowAnimation

        renameWindow?.showAtLocation(renameContainer, Gravity.TOP, 0, 0)
        dimBehindMenu(renameWindow)

        renameBar.focusAndShowKeyboard()

        closeButton.setOnClickListener {
            hideMenu(renameWindow)
        }

        fun rename(recoverOriginal: Boolean = false) {
            renameApp(app.packageName, if (recoverOriginal) app.originalName else renameText)
            blurAndHideKeyboard()
            hideMenu()
            onRenameCallback(ContextMenuItem.RENAME_APP)
        }

        renameBar.doAfterTextChanged {
            if (it.toString() == "") renameBar.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_rename,
                0,
                0,
                0
            ) else renameBar.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.icon_submit,
                0
            )

            renameText = it.toString()
        }

        renameBar.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                rename()
                return@OnEditorActionListener true
            }
            false
        })

        renameBar.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP && renameText != "") {
                val padding: Int = renameBar.paddingRight * 2
                val iconWidth: Int = renameBar.compoundDrawables[2].bounds.width()

                if (event.rawX >= renameBar.right - iconWidth - padding) rename()
            }
            view.performClick()
        }

        recoverOriginalNameButton.setOnClickListener {
            rename(true)
        }

    }


    private fun dimBehindMenu(menu: PopupWindow?) {
        if (menu == null) return

        val container = menu.contentView.rootView
        val context = menu.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.8f
        wm.updateViewLayout(container, p)
    }

    fun hideMenu(menu: PopupWindow? = null) {
        if (menu == null) {
            contextMenuWindow?.dismiss()
            renameWindow?.dismiss()
        } else menu.dismiss()
    }

    fun pxToDp(px: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    @Suppress("DEPRECATION")
    fun vibrate(timeInMs: Long = 64, amplitude: Int = 64) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(timeInMs, amplitude))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(timeInMs, amplitude))
        }
    }
}