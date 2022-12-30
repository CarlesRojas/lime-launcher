package app.pinya.lime

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager


class MainActivity : Activity() {

    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        state = State(this)

        linkAdapters()
    }

    private fun linkAdapters() {
        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        CustomPagerAdapter(this, state).also {
            viewPager.adapter = it
        }
    }
}