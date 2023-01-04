package app.pinya.lime

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener


class MainActivity : Activity() {

    private lateinit var state: State
    private lateinit var customPageAdapter: CustomPagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var dailyWallpaper: DailyWallpaper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        state = State(this)
        dailyWallpaper = DailyWallpaper(this, state)


        linkAdapters()
        dimBackground()
        updateStatusBarTextColor()
    }

    private fun linkAdapters() {
        viewPager = findViewById(R.id.viewPager)
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

        dailyWallpaper.updateWallpaper()
        state.fetchInstalledAppsAgain()
        state.hideMenu()
        viewPager.currentItem = 0
        customPageAdapter.onResume()
        dimBackground()
        updateStatusBarTextColor()
    }

    private fun dimBackground() {
        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)
        val dimBackgroundValue = state.getData(DataKey.DIM_BACKGROUND, true)

        viewPager.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (dimBackgroundValue) (if (blackTextValue) R.color.white_extra_low else R.color.black_extra_low) else R.color.transparent
            )
        )
    }

    private fun updateStatusBarTextColor() {
        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (blackTextValue) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    }
}