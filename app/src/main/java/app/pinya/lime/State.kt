package app.pinya.lime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class DataKey {
    DATE_FORMAT,
    TIME_FORMAT,
    AUTO_SHOW_KEYBOARD,
    AUTO_OPEN_APPS,
    ICONS_IN_HOME,
    ICONS_IN_DRAWER,
    SHOW_SEARCH_BAR,
    SHOW_ALPHABET_FILTER,
    HOME_APPS
}

class State(context: Context) {

    private val context: Context
    private val preferencesName = "LimeLauncherPreferences"
    private val sharedPreferences: SharedPreferences

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
        getInstalledApps()
        getHomeApps()
    }

    private fun getInstalledApps() {
        installedAppList.clear()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val untreatedAppList = context.packageManager.queryIntentActivities(intent, 0)

        for (untreatedApp in untreatedAppList) {
            val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
            val packageName = untreatedApp.activityInfo.packageName
            val icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
            val app = ItemApp(name, packageName, icon)

            if (!installedAppList.contains(app)) installedAppList.add(app)
        }

        installedAppList.sortBy { it.getName() }
    }

    private fun getHomeApps() {
        val homeApps: MutableSet<String>? = getData(DataKey.HOME_APPS, mutableSetOf()) ?: return

        if (homeApps != null) {
            for (homeApp in homeApps) {
                val app = installedAppList.find { it.getPackageName() == homeApp } ?: continue
                app.home = true
            }
        }
    }

    fun toggleHomeInApp(packageName: String, homeNewValue: Boolean) {
        val app = installedAppList.find { it.getPackageName() == packageName } ?: return

        app.home = homeNewValue

        val homeAppSet = mutableSetOf<String>()
        installedAppList.forEach {
            if (it.home) homeAppSet.add(it.getPackageName())
        }

        saveData(DataKey.HOME_APPS, homeAppSet)
    }

    fun isAppInHome(packageName: String): Boolean {
        val app = installedAppList.find { it.getPackageName() == packageName } ?: return false
        return app.home
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
        editor.putStringSet(key.toString(), data)
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

    fun getData(key: DataKey, defaultValue: MutableSet<String>): MutableSet<String>? {
        return this.sharedPreferences.getStringSet(key.toString(), defaultValue)
    }

    fun getData(key: DataKey, defaultValue: Float): Float {
        return this.sharedPreferences.getFloat(key.toString(), defaultValue)
    }

    fun getData(
        key: DataKey,
        defaultValue: MutableMap<String, String>
    ): MutableMap<String, String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
        val listType = object : TypeToken<MutableMap<String, String>>() {}.type
        return Gson().fromJson(result, listType)
    }


    fun showContextMenu(app: ItemApp, contextMenuContainer: ConstraintLayout) {
        val contextMenuView = View.inflate(context, R.layout.context_menu, null)
        val icon = contextMenuView.findViewById<ImageView>(R.id.appIcon)
        val appName = contextMenuView.findViewById<TextView>(R.id.appName)
        val close = contextMenuView.findViewById<ImageButton>(R.id.closeContextMenuButton)

        val addToHomeButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_addToHome)
        val removeFromHomeButton =
            contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_removeFromHome)

        icon.setImageDrawable(app.getIcon())
        val stateValue = getData(DataKey.ICONS_IN_DRAWER, true)
        icon.visibility = if (stateValue) View.VISIBLE else View.GONE
        appName.text = app.getName()

        addToHomeButton.visibility = if (app.home) View.GONE else View.VISIBLE
        removeFromHomeButton.visibility = if (app.home) View.VISIBLE else View.GONE

        val contextMenuWindow = PopupWindow(
            contextMenuView,
            contextMenuContainer.width - contextMenuContainer.paddingRight - contextMenuContainer.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow.showAtLocation(contextMenuContainer, Gravity.BOTTOM, 0, 0)

        close.setOnClickListener {
            contextMenuWindow.dismiss()
        }

        addToHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), true)
            showToast("${app.getName()} added to Home")
            contextMenuWindow.dismiss()
        }

        removeFromHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), false)
            showToast("${app.getName()} removed from Home")
            contextMenuWindow.dismiss()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}