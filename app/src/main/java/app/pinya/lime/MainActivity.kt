package app.pinya.lime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager


class MainActivity : Activity() {

    private val installedAppList: MutableList<ItemApp> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getInstalledApps()
        linkAdapters()
    }

    private fun getInstalledApps() {
        installedAppList.clear()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val untreatedAppList = this.packageManager.queryIntentActivities(intent, 0)

        for (untreatedApp in untreatedAppList) {
            val name = untreatedApp.activityInfo.loadLabel(packageManager).toString()
            val packageName = untreatedApp.activityInfo.packageName
            val icon = untreatedApp.activityInfo.loadIcon(packageManager)
            val app = ItemApp(name, packageName, icon)

            if (!installedAppList.contains(app)) installedAppList.add(app)
        }
    }


    private fun linkAdapters() {
        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        CustomPagerAdapter(this, installedAppList).also {
            viewPager.adapter = it
        }
    }
}