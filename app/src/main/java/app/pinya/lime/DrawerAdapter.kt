package app.pinya.lime

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.floor


val ALPHABET: List<Char> = listOf(
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
    'G',
    'H',
    'I',
    'J',
    'K',
    'L',
    'M',
    'N',
    'O',
    'P',
    'Q',
    'R',
    'S',
    'T',
    'U',
    'V',
    'W',
    'X',
    'Y',
    'Z'
)

class DrawerAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private var searchBar: EditText? = null
    private var appList: RecyclerView? = null
    private var drawerConstraintLayout: ConstraintLayout? = null
    private var alphabetLayout: LinearLayout? = null
    private var contextMenuContainer: ConstraintLayout? = null

    private var searchBarText: String = ""
    private var filteringByAlphabet = false
    private var lastFilterWasAlphabet = false

    private var shownAppList: MutableList<ItemApp> = mutableListOf()
    private val currentAlphabet: MutableList<Char> = mutableListOf()

    init {
        this.context = context
        this.state = state
        this.layout = layout

        filterAppList()
        initSearchBar()
        initContextMenu()
        initLayout()
        initAppList()
        initAlphabet()
        showHideElements()
    }

    fun onResume() {
        clearText()
        hideKeyboard()
        filterAppList()
        initAlphabet()
        showHideElements()
    }

    fun showHideElements() {
        val showSearchBar = state.getData(DataKey.SHOW_SEARCH_BAR, true)
        val showAlphabetFilter = state.getData(DataKey.SHOW_ALPHABET_FILTER, true)

        searchBar?.visibility = if (showSearchBar) View.VISIBLE else View.GONE
        alphabetLayout?.visibility = if (showAlphabetFilter) View.VISIBLE else View.GONE

        val constraintSet = ConstraintSet()
        constraintSet.clone(drawerConstraintLayout)

        constraintSet.connect(
            R.id.appListConstraintLayout,
            ConstraintSet.TOP,
            if (showSearchBar) R.id.guidelineH2 else R.id.guidelineH1,
            ConstraintSet.TOP,
            0
        )

        constraintSet.applyTo(drawerConstraintLayout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchBar() {
        searchBar = layout.findViewById(R.id.drawerSearchBar)

        searchBar?.doAfterTextChanged {
            if (it.toString() == "") hideClearText() else showClearText()
            searchBarText = it.toString()
            if (filteringByAlphabet) filterAppListByAlphabet() else filterAppList()
            filteringByAlphabet = false
            lastFilterWasAlphabet = false
        }

        searchBar?.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP && searchBarText != "") {
                val padding: Int = searchBar!!.paddingRight * 2
                val iconWidth: Int = searchBar!!.compoundDrawables[2].bounds.width()

                if (event.rawX >= searchBar!!.right - iconWidth - padding) clearText()
            }

            view.performClick()
        }
    }

    private fun initContextMenu() {
        contextMenuContainer = layout.findViewById(R.id.contextMenuDrawer_parent)
    }

    private fun initLayout() {
        layout.setOnClickListener {
            hideKeyboard()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAppList() {
        drawerConstraintLayout = layout.findViewById(R.id.drawerConstraintLayout)
        appList = layout.findViewById(R.id.drawerAppList)

        appList?.setOnTouchListener { view, _ ->
            hideKeyboard()
            view.performClick()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initAlphabet() {
        alphabetLayout = layout.findViewById(R.id.alphabetLayout)
        updateAlphabetLetters()

        alphabetLayout?.setOnTouchListener { _, event ->
            clearText()
            hideKeyboard()
            val startY = alphabetLayout!!.top
            val endY = alphabetLayout!!.bottom
            val perc = (event.rawY - startY) / (endY - startY)
            val letterHeight = 1f / (ALPHABET.size + 1)
            val currentSection = floor(perc / letterHeight).toInt()

            if (currentSection >= 0 && currentSection < currentAlphabet.size) {
                val currentChar = currentAlphabet[currentSection]
                filteringByAlphabet = true
                searchBar?.setText(currentChar.toString())
                lastFilterWasAlphabet = true
            }

            true
        }
    }

    private fun updateAlphabetLetters() {
        currentAlphabet.clear()

        for (app in state.getInstalledAppList()) {
            val char = app.name.first().uppercaseChar()

            if (currentAlphabet.contains(char)) continue
            if (ALPHABET.contains(char)) currentAlphabet.add(char)
            else if (!currentAlphabet.contains('#')) currentAlphabet.add(0, '#')
        }

        alphabetLayout?.removeAllViews()
        val alphabetHeight = alphabetLayout?.height ?: 20
        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)

        for (char in currentAlphabet) {
            val textView = View.inflate(context, R.layout.view_alphabet_character, null) as TextView
            textView.text = char.toString()
            textView.height = alphabetHeight / (ALPHABET.size + 1)
            textView.typeface = ResourcesCompat.getFont(context, R.font.montserrat)
            textView.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (blackTextValue) R.color.black else R.color.white
                )
            )
            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            textView.setAutoSizeTextTypeUniformWithConfiguration(
                8, 14, 1, TypedValue.COMPLEX_UNIT_SP
            )
            alphabetLayout?.addView(textView)
        }
    }

    fun clearText() {
        searchBar?.text?.clear()
    }

    fun showKeyboard() {
        val autoOpenKeyboard = state.getData(DataKey.AUTO_SHOW_KEYBOARD, true)

        if (autoOpenKeyboard) {
            searchBar?.requestFocus()
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        searchBar?.clearFocus()

        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (searchBar != null) inputManager.hideSoftInputFromWindow(searchBar!!.windowToken, 0)
    }

    private fun showClearText() {
        searchBar?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_close, 0)
    }

    private fun hideClearText() {
        searchBar?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_search, 0, 0, 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterAppList() {
        shownAppList = mutableListOf()

        val installedList = this.state.getInstalledAppList()
        installedList.forEach {
            val included =
                if (searchBarText == "") true else it.name.contains(searchBarText, true)

            if (included) shownAppList.add(it)
        }

        val autoOpenApps = state.getData(DataKey.AUTO_OPEN_APPS, true)
        val moreThanOneInstalledApp = installedList.size > 1

        if (autoOpenApps && moreThanOneInstalledApp && shownAppList.size == 1) {
            val app = shownAppList[0]
            clearText()
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        this.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterAppListByAlphabet() {
        shownAppList = mutableListOf()
        this.state.getInstalledAppList().forEach {
            val included = when (searchBarText) {
                "" -> true
                "#" -> !ALPHABET.contains(it.name.first().uppercaseChar())
                else -> it.name.startsWith(searchBarText, true)
            }

            if (included) shownAppList.add(it)
        }

        this.notifyDataSetChanged()
    }


    private fun onContextMenuClick(item: ContextMenuItem) {
        when (item) {
            ContextMenuItem.HIDE_APP -> {
                if (lastFilterWasAlphabet) filterAppListByAlphabet() else filterAppList()
                initAlphabet()
            }
            ContextMenuItem.SHOW_APP -> {
                if (lastFilterWasAlphabet) filterAppListByAlphabet() else filterAppList()
                initAlphabet()
            }
            ContextMenuItem.RENAME_APP -> {
                if (lastFilterWasAlphabet) filterAppListByAlphabet() else filterAppList()
                initAlphabet()
            }
            else -> {}
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = shownAppList[position]

        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.icon)
        val stateValue = state.getData(DataKey.ICONS_IN_DRAWER, true)
        imageView.visibility = if (stateValue) View.VISIBLE else View.GONE

        textView.text = currentApp.name
        textView.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )

        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.packageName)

            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        linearLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) hideKeyboard()
            false
        }

        if (contextMenuContainer != null)
            linearLayout.setOnLongClickListener {
                state.showContextMenu(
                    currentApp,
                    contextMenuContainer!!,
                    false,
                    ::onContextMenuClick
                )
                true
            }
    }

    override fun getItemCount(): Int {
        return shownAppList.size
    }
}