package app.pinya.lime

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener


class MainActivity : Activity() {

    private lateinit var state: State
    private lateinit var customPageAdapter: CustomPagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        state = State(this)

        linkAdapters()
    }

    private fun linkAdapters() {
        viewPager = findViewById<ViewPager>(R.id.viewPager)
        customPageAdapter = CustomPagerAdapter(this, state).also {
            viewPager.adapter = it

            viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0) it.onHomePageSelected() else it.onDrawerPageSelected()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    override fun onResume() {
        super.onResume()

        state.fetchInstalledAppsAgain()
        viewPager.currentItem = 0
        customPageAdapter.onResume()
    }
}