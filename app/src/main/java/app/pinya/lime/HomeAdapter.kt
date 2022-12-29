package app.pinya.lime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class HomeAdapter(installedAppList: MutableList<ItemApp>) :
    RecyclerView.Adapter<ItemAppViewHolder>() {
    private val installedAppList: MutableList<ItemApp>

    init {
        this.installedAppList = installedAppList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.item_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = this.installedAppList[position]

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)

        imageView.setImageDrawable(currentApp.getIcon())
        textView.text = currentApp.getName()
    }

    override fun getItemCount(): Int {
        return this.installedAppList.size
    }
}