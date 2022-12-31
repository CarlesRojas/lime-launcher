package app.pinya.lime

import OnSwipeTouchListener
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
import kotlinx.coroutines.*


class DrawerAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private lateinit var searchBar: EditText
    private lateinit var appList: RecyclerView

    private var searchBarText: String = ""


    init {
        this.context = context
        this.state = state
        this.layout = layout

        initSearchBar()
        initLayout()
        initAppList()
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

        layout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onAnyTouch() {
                hideKeyboard()
            }

            override fun onSwipeBottom() {
                expandNotificationBar()
            }
        })
    }

    var showNotificationsJob: Job? = null

    private fun initAppList() {
        appList = layout.findViewById(R.id.drawerAppList)

        appList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return

                if (!recyclerView.canScrollVertically(-1)) {
                    showNotificationsJob = GlobalScope.launch(Dispatchers.Main) {
                        delay(50)
                        expandNotificationBar()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy >= 0) showNotificationsJob?.cancel()
            }
        })
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    private fun expandNotificationBar() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Error) {
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
        this.state.getInstalledAppList().forEach {
            it.filtered =
                if (searchBarText == "") false else !it.getName().contains(searchBarText, true)
        }

        val numAppsInTheList = itemCount
        val autoOpenApps = state.getData(DataKey.AUTO_OPEN_APPS, true)

        if (autoOpenApps && numAppsInTheList == 1) {
            val app = this.state.getInstalledAppList().find { !it.filtered }

            if (app != null) {
                clearText()
                val launchAppIntent =
                    context.packageManager.getLaunchIntentForPackage(app.getPackageName())
                if (launchAppIntent != null) context.startActivity(launchAppIntent)
            }
        }

        indexDisplacement = 0;
        this.notifyDataSetChanged();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.item_app, parent, false)
        )
    }

    var indexDisplacement = 0
    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        var currentApp: ItemApp? = null
        while (currentApp == null) {
            val newApp = this.state.getInstalledAppList()[position + indexDisplacement]
            if (newApp.filtered) indexDisplacement++
            else currentApp = newApp
        }

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        imageView.setImageDrawable(currentApp.getIcon())
        textView.text = currentApp.getName()
        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.getPackageName())

            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }
    }

    override fun getItemCount(): Int {
        return this.state.getInstalledAppList().filter { !it.filtered }.size
    }
}