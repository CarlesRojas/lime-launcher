package app.pinya.lime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    ADD_TO_HOME, REMOVE_FROM_HOME, HIDE_APP, SHOW_APP
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

        val hiddenApps: MutableSet<String>? = getData(DataKey.HIDDEN_APPS, mutableSetOf())
        val homeApps: MutableSet<String>? = getData(DataKey.HOME_APPS, mutableSetOf())
        val showHiddenApps: Boolean = getData(DataKey.SHOW_HIDDEN_APPS, false)

        for (untreatedApp in untreatedAppList) {
            val name = untreatedApp.activityInfo.loadLabel(context.packageManager).toString()
            val packageName = untreatedApp.activityInfo.packageName
            val icon = untreatedApp.activityInfo.loadIcon(context.packageManager)
            val app = ItemApp(name, packageName, icon)

            app.hidden = hiddenApps?.find { it == packageName } != null
            app.home = homeApps?.find { it == packageName } != null

            if (!showHiddenApps && app.hidden) continue
            if (!installedAppList.contains(app)) installedAppList.add(app)
        }

        installedAppList.sortBy { it.getName() }
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

        saveData(DataKey.HOME_APPS, homeAppSet)
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
        onClickCallback: (item: ContextMenuItem) -> Unit = { }
    ) {
        val contextMenuView = View.inflate(context, R.layout.context_menu, null)
        val icon = contextMenuView.findViewById<ImageView>(R.id.appIcon)
        val appName = contextMenuView.findViewById<TextView>(R.id.appName)
        val close = contextMenuView.findViewById<ImageButton>(R.id.closeContextMenuButton)

        val addToHomeButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_addToHome)
        val removeFromHomeButton =
            contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_removeFromHome)
        val showAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_showApp)
        val hideAppButton = contextMenuView.findViewById<LinearLayout>(R.id.contextMenu_hideApp)

        icon.setImageDrawable(app.getIcon())
        appName.text = app.getName()

        addToHomeButton.visibility = if (app.home) View.GONE else View.VISIBLE
        removeFromHomeButton.visibility = if (app.home) View.VISIBLE else View.GONE
        showAppButton.visibility = if (app.hidden) View.VISIBLE else View.GONE
        hideAppButton.visibility = if (app.hidden) View.GONE else View.VISIBLE

        contextMenuWindow = PopupWindow(
            contextMenuView,
            contextMenuContainer.width - contextMenuContainer.paddingRight - contextMenuContainer.paddingLeft,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        contextMenuWindow?.showAtLocation(contextMenuContainer, Gravity.BOTTOM, 0, 0)
        dimBehind()

        close.setOnClickListener {
            hideContextMenu()
        }

        addToHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), true)
            onClickCallback(ContextMenuItem.ADD_TO_HOME)
            showToast("${app.getName()} added to Home")
            hideContextMenu()
        }

        removeFromHomeButton.setOnClickListener {
            toggleHomeInApp(app.getPackageName(), false)
            onClickCallback(ContextMenuItem.REMOVE_FROM_HOME)
            showToast("${app.getName()} removed from Home")
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

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}