package app.pinya.lime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class HomeAdapter(context: Context, appList: MutableList<ItemApp>): BaseAdapter() {
    private val context: Context
    private val appList: MutableList<ItemApp>

    init{
        this.context = context
        this.appList = appList
    }

    override fun getCount(): Int {
        return this.appList.size
    }

    override fun getItem(position: Int): Any {
        return this.appList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view: View = convertView ?: inflater.inflate(R.layout.item_app, parent, false)

        val imageView: ImageView = view.findViewById(R.id.appIcon)
        val textView: TextView = view.findViewById(R.id.appName)

        imageView.setImageDrawable(this.appList[position].getIcon())
        textView.text = this.appList[position].getName()

        return view
    }
}