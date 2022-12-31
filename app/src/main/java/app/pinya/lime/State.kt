package app.pinya.lime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class DataKey {
    DATE_FORMAT,
    TIME_FORMAT,
    AUTO_SHOW_KEYBOARD,
    AUTO_OPEN_APPS,
    ICONS_IN_HOME,
    ICONS_IN_DRAWER
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

        fetchInstalledAppsAgain()
    }

    fun getInstalledAppList(): MutableList<ItemApp> {
        return this.installedAppList
    }

    fun fetchInstalledAppsAgain() {
        getInstalledApps()
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

    fun getData(
        key: DataKey,
        defaultValue: MutableMap<String, String>
    ): MutableMap<String, String> {
        val jsonDefaultValue: String = Gson().toJson(defaultValue)
        val result = this.sharedPreferences.getString(key.toString(), jsonDefaultValue)
        val listType = object : TypeToken<MutableMap<String, String>>() {}.type
        return Gson().fromJson(result, listType)
    }

}