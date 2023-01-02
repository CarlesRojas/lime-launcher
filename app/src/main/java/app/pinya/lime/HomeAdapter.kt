package app.pinya.lime

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ItemAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class HomeAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private lateinit var contextMenuContainer: ConstraintLayout

    private val date: TextView
    private val time: TextView

    private var timer: Timer? = Timer()

    private var homeAppList: List<ItemApp> = mutableListOf()

    init {
        this.context = context
        this.state = state
        this.layout = layout

        date = layout.findViewById(R.id.homeDate)
        time = layout.findViewById(R.id.homeTime)

        startTimerToUpdateDateTime()
        initContextMenu()
        initGestureDetector()
        getHomeAppList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onResume() {
        this.notifyDataSetChanged()
        getHomeAppList()
    }

    private fun startTimerToUpdateDateTime() {

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (context as Activity).runOnUiThread {
                    val dateStateValue = state.getData(DataKey.DATE_FORMAT, 1)
                    val timeStateValue = state.getData(DataKey.TIME_FORMAT, 0)

                    date.text =
                        SimpleDateFormat.getDateInstance(SettingsActivity.mapFormat(dateStateValue))
                            .format(Date())
                    time.text =
                        SimpleDateFormat.getTimeInstance(SettingsActivity.mapFormat(timeStateValue))
                            .format(Date())
                }
            }
        }, 0, 1000)
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    private fun expandNotificationBar() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (_: Error) {
        }
    }


    private fun initContextMenu() {
        contextMenuContainer = layout.findViewById(R.id.contextMenuHome_parent)
    }

    private fun initGestureDetector() {
        layout.setOnLongClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getHomeAppList() {
        homeAppList = this.state.getInstalledAppList().filter {
            it.home
        }

        this.notifyDataSetChanged()
    }

    private fun onContextMenuClick(item: ContextMenuItem) {
        when (item) {
            ContextMenuItem.REMOVE_FROM_HOME -> getHomeAppList()
            ContextMenuItem.HIDE_APP -> getHomeAppList()
            ContextMenuItem.SHOW_APP -> getHomeAppList()
            else -> {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAppViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemAppViewHolder(
            inflater.inflate(R.layout.view_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemAppViewHolder, position: Int) {
        val currentApp = homeAppList[position]

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.getIcon())
        val stateValue = state.getData(DataKey.ICONS_IN_HOME, true)
        imageView.visibility = if (stateValue) View.VISIBLE else View.GONE

        textView.text = currentApp.getName()

        linearLayout.setOnClickListener {
            val launchAppIntent =
                context.packageManager.getLaunchIntentForPackage(currentApp.getPackageName())

            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }

        linearLayout.setOnLongClickListener {
            state.showContextMenu(currentApp, contextMenuContainer, ::onContextMenuClick)
            true
        }
    }

    override fun getItemCount(): Int {
        return homeAppList.size
    }
}