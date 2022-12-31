package app.pinya.lime

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter


class CustomPagerAdapter(context: Context, state: State) :
    PagerAdapter() {
    private val context: Context
    private val state: State

    private lateinit var home: HomeAdapter
    private lateinit var drawer: DrawerAdapter

    init {
        this.context = context
        this.state = state
    }

    fun onHomePageSelected() {
        this.drawer.hideKeyboard()
        this.drawer.clearText()
    }

    fun onDrawerPageSelected() {
        this.drawer.showKeyboard()
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        return if (position == 0) instantiateHome(collection)
        else instantiateDrawer(collection)
    }

    private fun instantiateHome(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_home, collection, false) as ViewGroup
        val viewHome = layout.findViewById<View>(R.id.homeAppList) as RecyclerView

        this.home = HomeAdapter(context, state, layout).also {
            viewHome.adapter = it
            viewHome.layoutManager = LinearLayoutManager(context)
        }

        collection.addView(layout)
        return layout
    }

    private fun instantiateDrawer(collection: ViewGroup): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_drawer, collection, false) as ViewGroup
        val viewDrawer = layout.findViewById<View>(R.id.drawerAppList) as RecyclerView

        this.drawer = DrawerAdapter(context, state, layout).also {
            viewDrawer.adapter = it
            viewDrawer.layoutManager = LinearLayoutManager(context)
        }

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0) "Home" else "Drawer"
    }

}