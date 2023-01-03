package app.pinya.lime

import android.app.Activity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener


class MainActivity : Activity() {

    private lateinit var state: State
    private lateinit var customPageAdapter: CustomPagerAdapter
    private lateinit var viewPager: ViewPager

    private var currentTextIsBlack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        state = State(this)
        applyStyle()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linkAdapters()
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
        applyStyle()

        super.onResume()

        state.fetchInstalledAppsAgain()
        state.hideMenu()
        viewPager.currentItem = 0
        customPageAdapter.onResume()
    }

    private fun applyStyle() {
        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)
        if (blackTextValue != currentTextIsBlack) {
            theme.applyStyle(if (blackTextValue) R.style.BlackText else R.style.WhiteText, true)
            currentTextIsBlack = blackTextValue
            recreate()
        }
    }
}