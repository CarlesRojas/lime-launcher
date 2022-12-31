package app.pinya.lime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView


class DrawerAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private lateinit var searchBar: EditText
    private lateinit var appList: RecyclerView

    private var searchBarText: String = ""

    private var shownAppList: MutableList<ItemApp> = mutableListOf()

    init {
        this.context = context
        this.state = state
        this.layout = layout

        filterAppList()
        initSearchBar()
        initLayout()
        initAppList()
    }

    fun onResume() {
        filterAppList()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchBar() {
        searchBar = layout.findViewById(R.id.drawerSearchBar)

        searchBar.doAfterTextChanged {
            if (it.toString() == "") hideClearText() else showClearText()
            searchBarText = it.toString()
            filterAppList()
        }

        searchBar.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP && searchBarText != "") {
                val padding: Int = searchBar.paddingRight * 2
                val iconWidth: Int = searchBar.compoundDrawables[2].bounds.width()

                if (event.rawX >= searchBar.right - iconWidth - padding) clearText()
            }

            view.performClick()
        }
    }

    private fun initLayout() {
        layout.setOnClickListener {
            hideKeyboard()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAppList() {
        appList = layout.findViewById(R.id.drawerAppList)

        appList.setOnTouchListener { view, _ ->
            hideKeyboard()
            view.performClick()
        }
    }

    fun clearText() {
        searchBar.text.clear()
    }

    fun showKeyboard() {
        val autoOpenKeyboard = state.getData(DataKey.AUTO_SHOW_KEYBOARD, true)

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

    private fun showClearText() {
        searchBar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_line, 0)
    }

    private fun hideClearText() {
        searchBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_line, 0, 0, 0)
    }

    private fun filterAppList() {
        shownAppList = mutableListOf()
        this.state.getInstalledAppList().forEach {
            val included =
                if (searchBarText == "") true else it.getName().contains(searchBarText, true)

            if (included) shownAppList.add(it)
        }

        val autoOpenApps = state.getData(DataKey.AUTO_OPEN_APPS, true)
        val moreThanOneInstalledApp = this.state.getInstalledAppList().size > 1

        if (autoOpenApps && moreThanOneInstalledApp && shownAppList.size == 1) {
            val app = shownAppList[0]
            clearText()
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(app.getPackageName())
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        this.notifyDataSetChanged();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.item_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = shownAppList[position]

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        imageView.setImageDrawable(currentApp.getIcon())

        val stateValue = state.getData(DataKey.ICONS_IN_DRAWER, true)
        imageView.visibility = if (stateValue) View.VISIBLE else View.GONE

        textView.text = currentApp.getName()

        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.getPackageName())

            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }
    }

    override fun getItemCount(): Int {
        return shownAppList.size
    }
}