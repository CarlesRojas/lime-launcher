package app.pinya.lime

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager


class MainActivity : AppCompatActivity() {

    private val appList: MutableList<ItemApp> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linkAdapters()
    }

    private fun linkAdapters() {
        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        viewPager.adapter = CustomPagerAdapter(this)


        val viewHome = findViewById<View>(R.id.homeListView) as ListView
        viewHome.adapter = HomeAdapter(this, appList )
    }
}