package app.pinya.lime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter


class CustomPagerAdapter(context: Context, installedAppList: MutableList<ItemApp>) :
    PagerAdapter() {
    private val context: Context
    private val installedAppList: MutableList<ItemApp>

    init {
        this.context = context
        this.installedAppList = installedAppList
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)

        if (position == 0) {
            val layout = inflater.inflate(R.layout.view_home, collection, false) as ViewGroup
            val viewHome = layout.findViewById<View>(R.id.homeRecyclerView) as RecyclerView

            HomeAdapter(context, installedAppList).also {
                viewHome.adapter = it
                viewHome.layoutManager = LinearLayoutManager(context)
            }
            collection.addView(layout)
            return layout

        } else {
            val layout = inflater.inflate(R.layout.view_drawer, collection, false) as ViewGroup
            val viewDrawer = layout.findViewById<View>(R.id.drawerRecyclerView) as RecyclerView

            DrawerAdapter(context, installedAppList).also {
                viewDrawer.adapter = it
                viewDrawer.layoutManager = LinearLayoutManager(context)
            }
            collection.addView(layout)
            return layout
        }
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