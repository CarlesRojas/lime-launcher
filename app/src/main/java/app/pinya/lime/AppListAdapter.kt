package app.pinya.lime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    context: Context,
    state: State,
    layout: ViewGroup,
    isTime: Boolean,
    onChangeAppCallback: (useDefault: Boolean, useNone: Boolean, newAppPackage: String) -> Unit
) : RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup
    private val isTime: Boolean
    private val onChangeAppCallback: (useDefault: Boolean, useNone: Boolean, newAppPackage: String) -> Unit

    init {
        this.context = context
        this.state = state
        this.layout = layout
        this.isTime = isTime
        this.onChangeAppCallback = onChangeAppCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        when (position) {
            0 -> {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, if (isTime) R.drawable.icon_clock else R.drawable.icon_calendar
                    )
                )
                textView.text = if (isTime) "Default clock app" else "Default calendar app"

                linearLayout.setOnClickListener {
                    onChangeAppCallback(true, false, "")
                }

            }
            1 -> {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.icon_close
                    )
                )
                textView.text = "Don't open any app"

                linearLayout.setOnClickListener {
                    onChangeAppCallback(false, true, "")
                }
            }
            else -> {
                val currentApp = state.getComleteAppList()[position + 2]

                imageView.setImageDrawable(currentApp.icon)
                textView.text = currentApp.name

                linearLayout.setOnClickListener {
                    onChangeAppCallback(false, false, currentApp.packageName)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return state.getComleteAppList().size + 2
    }
}