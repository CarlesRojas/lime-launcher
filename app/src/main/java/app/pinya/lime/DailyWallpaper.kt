package app.pinya.lime

import android.app.WallpaperManager
import android.content.Context
import java.net.URL
import java.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.concurrent.thread

const val BING_WALLPAPER_URL = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1"

class DailyWallpaper(context: Context, state: State) {

    private val context: Context
    private val state: State

    init {
        this.context = context
        this.state = state
    }

    fun updateWallpaper() {
        val dailyWallpaperActive = state.getData(DataKey.DAILY_WALLPAPER, false)
        if (!dailyWallpaperActive) return
        
        val wallpaperDate = state.getData(DataKey.WALLPAPER_DATE, -1)

        val cal: Calendar = Calendar.getInstance()
        val date = cal.get(Calendar.DATE)

        if (wallpaperDate != date) fetchWallpaper()
    }

    private fun String.removeAll(charactersToRemove: Set<Char>): String {
        return filterNot { charactersToRemove.contains(it) }
    }

    private fun fetchWallpaper() {

        val thread = Thread {
            try {
                val apiResponse = URL(BING_WALLPAPER_URL).readText()
                val jsonObject = Json.parseToJsonElement(apiResponse).jsonObject
                val images = jsonObject["images"] as JsonArray

                if (images.size > 0) {
                    val image: JsonObject = images[0].jsonObject
                    val url = "https://bing.com${image["url"].toString().removeAll(setOf('"'))}"
                    setWallpaper(url)
                }

            } catch (_: Exception) {
            }
        }

        thread.start()
    }

    private fun setWallpaper(url: String) {
        thread(start = true) {
            try {
                val inputStream = URL(url).openStream()
                WallpaperManager.getInstance(context).setStream(inputStream)

                val cal: Calendar = Calendar.getInstance()
                val date = cal.get(Calendar.DATE)
                state.saveData(DataKey.WALLPAPER_DATE, date)
            } catch (_: Exception) {
            }
        }
    }

}