package app.pinya.lime

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class DrawerAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private lateinit var searchBar: EditText

    init {
        this.context = context
        this.state = state
        this.layout = layout

        initSearchBar()
    }

    private fun initSearchBar() {
        this.searchBar = layout.findViewById(R.id.drawerSearchBar)
        searchBar.setOnFocusChangeListener { _, focus ->
            print(focus)
        }
    }

    fun showKeyboard() {
        val autoOpenKeyboard = state.getData(DataKey.AUTOSHOW_KEYBOARD, true)

        if (autoOpenKeyboard) {
            searchBar.requestFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        searchBar.clearFocus()

        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(searchBar.windowToken, 0)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.item_app, parent, false)
        )

    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = this.state.getInstalledAppList()[position]

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        imageView.setImageDrawable(currentApp.getIcon())
        textView.text = currentApp.getName()
        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.getPackageName())

            if (launchAppIntent !== null) context.startActivity(launchAppIntent)
        }
    }

    override fun getItemCount(): Int {
        return this.state.getInstalledAppList().size
    }
}