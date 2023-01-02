package app.pinya.lime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


enum class DataKey {
    DATE_FORMAT, TIME_FORMAT, AUTO_SHOW_KEYBOARD, AUTO_OPEN_APPS, ICONS_IN_HOME, ICONS_IN_DRAWER, SHOW_SEARCH_BAR, SHOW_ALPHABET_FILTER, HOME_APPS, HIDDEN_APPS, SHOW_HIDDEN_APPS
}

enum class ContextMenuItem {
    ADD_TO_HOME, REMOVE_FROM_HOME, HIDE_APP, SHOW_APP, APP_INFO, UNINSTALL, MOVE_UP, MOVE_DOWN
}

class State(context: Context) {

    private val context: Context
    private val preferencesName = "LimeLauncherPreferences"
    private val sharedPreferences: SharedPreferences

    private var contextMenuWindow: PopupWindow? = null

    private var installedAppList: MutableList<ItemApp> = mutableListOf()

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

        val hiddenApps: MutableSet<String>? = getData(DataKey.HIDDEN_APPS, mutableSetOf())
        val homeApps: MutableSet<String>? = getData(DataKey.HOME_APPS, mutableSetOf())
        val showHiddenApps: Boolean = getData(DataKey.SHOW_HIDDEN_APPS, false)

        for (untreatedApp in untreatedAppList) {
            val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
            val packageName = untreatedApp.activityInfo.packageName
            val icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
            val app = ItemApp(name, packageName, icon)

            val packageInfo = packagesInfo.find { it.packageName == packageName }

            app.system =
                if (packageInfo != null) (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM else false
            app.hidden = hiddenApps?.find { it == packageName } != null
            app.home = homeApps?.find { it == packageName } != null

            if (!showHiddenApps && app.hidden) continue
            if (!installedAppList.contains(app)) installedAppList.add(app)
        }

        hiddenApps?.removeAll { app -> installedAppList.find { app == it.getPackageName() } == null }
        homeApps?.removeAll { app -> installedAppList.find { app == it.getPackageName() } == null }

        homeApps?.forEachIndexed { i, packageName ->
            val app = installedAppList.find { packageName == it.getPackageName() }
            if (app != null) app.homeOrderIndex = i
        }

        installedAppList.sortBy { it.getName().lowercase() }
    }

    private fun <T> MutableSet<T>.swap(index1: Int, index2: Int) {
        val list = this.toMutableList()

        val tmp = list[index1]
        list[index1] = list[index2]
        list[index2] = tmp
        this.clear()

        list.forEach { this.add(it) }
    }

    private fun toggleHomeInApp(packageName: String, homeNewValue: Boolean) {
        val app = installedAppList.find { it.getPackageName() == packageName }
        app?.home = homeNewValue

        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf()) ?: mutableSetOf()

        when (homeNewValue) {
            true -> homeAppSet.add(packageName)
            false -> homeAppSet.remove(packageName)
        }

        updateHomeAppOrder(homeAppSet)
        saveData(DataKey.HOME_APPS, homeAppSet)
    }

    private fun changeHomeAppOrder(packageName: String, moveUp: Boolean) {
        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf()) ?: mutableSetOf()

        var index = -1;
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
            val app = installedAppList.find { packageName == it.getPackageName() }
            if (app != null) app.homeOrderIndex = i
        }
    }

    private fun toggleHiddenApp(packageName: String, hiddenNewValue: Boolean) {
        val app = installedAppList.find { it.getPackageName() == packageName }
        app?.hidden = hiddenNewValue

        val hiddenAppsSet: MutableSet<String> =
            getData(DataKey.HIDDEN_APPS, mutableSetOf()) ?: mutableSetOf()

        when (hiddenNewValue) {
            true -> hiddenAppsSet.add(packageName)
            false -> hiddenAppsSet.remove(packageName)
        }

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

    fun getData(key: DataKey, defaultValue: MutableSet<String>): MutableSet<String>? {
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

        val homeAppSet: MutableSet<String> =
            getData(DataKey.HOME_APPS, mutableSetOf()) ?: mutableSetOf()

        val moveUpButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_moveUp)
        val moveDownButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_moveDown)
        val addToHomeButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_addToHome)
        val removeFromHomeButton =
            contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_removeFromHome)
        val showAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_showApp)
        val hideAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_hideApp)
        val appInfoButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_appInfo)
        val uninstallButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_uninstall)

        icon.setImageDrawable(app.getIcon())
        appName.text = app.getName()

        moveUpButton.visibility =
            if (!fromHome || app.homeOrderIndex <= 0) View.GONE else View.VISIBLE
        moveDownButton.visibility =
            if (!fromHome || app.homeOrderIndex >= homeAppSet.size - 1) View.GONE else View.VISIBLE
        addToHomeButton.visibility = if (app.home) View.GONE else View.VISIBLE
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

        contextMenuWindow?.showAtLocation(contextMenuContainer, Gravity.BOTTOM, 0, 0)
        dimBehind()

        closeButton.setOnClickListener {
            hideContextMenu()
        }

        settingsButton.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }

        moveUpButton.setOnClickListener {
            changeHomeAppOrder(app.getPackageName(), true)
            onClickCallback(ContextMenuItem.MOVE_UP)
            hideContextMenu()
        }

        moveDownButton.setOnClickListener {
            changeHomeAppOrder(app.getPackageName(), false)
            onClickCallback(ContextMenuItem.MOVE_DOWN)
            hideContextMenu()
        }

        addToHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), true)
            onClickCallback(ContextMenuItem.ADD_TO_HOME)
            hideContextMenu()
        }

        removeFromHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), false)
            onClickCallback(ContextMenuItem.REMOVE_FROM_HOME)
            hideContextMenu()
        }

        showAppButton.setOnClickListener {
            toggleHiddenApp(app.getPackageName(), false)
            onClickCallback(ContextMenuItem.SHOW_APP)
            hideContextMenu()
        }

        hideAppButton.setOnClickListener {
            toggleHiddenApp(app.getPackageName(), true)
            onClickCallback(ContextMenuItem.HIDE_APP)
            hideContextMenu()
        }

        appInfoButton.setOnClickListener {
            onClickCallback(ContextMenuItem.APP_INFO)
            hideContextMenu()

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + app.getPackageName())
            context.startActivity(intent)
        }

        uninstallButton.setOnClickListener {
            onClickCallback(ContextMenuItem.UNINSTALL)
            hideContextMenu()

            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:" + app.getPackageName())
            context.startActivity(intent)
        }
    }

    private fun dimBehind() {
        if (contextMenuWindow == null) return

        val container = contextMenuWindow!!.contentView.rootView
        val context = contextMenuWindow!!.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.8f
        wm.updateViewLayout(container, p)
    }

    fun hideContextMenu() {
        contextMenuWindow?.dismiss()
    }
}