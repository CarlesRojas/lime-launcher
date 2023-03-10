package app.pinya.lime

import OnSwipeTouchListener
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class ItemAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class HomeAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup

    private var contextMenuContainer: ConstraintLayout? = null

    private var date: TextView? = null
    private var time: TextView? = null

    private var timer: Timer? = Timer()
    private var homeAppList: List<ItemApp> = mutableListOf()

    init {
        this.context = context
        this.state = state
        this.layout = layout

        initDateTime()
        startTimerToUpdateDateTime()
        initContextMenu()
        initGestureDetector()
        getHomeAppList()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun onResume() {
        getHomeAppList()
        updateTimeDateStyle()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDateTime() {
        date = layout.findViewById(R.id.homeDate)
        date?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onClick() {
                when (val stateValue = state.getData(DataKey.DATE_CLICK_APP, "default")) {
                    "default" -> {
                        val builder: Uri.Builder =
                            CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                        val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(stateValue)
                        if (launchAppIntent != null) context.startActivity(launchAppIntent)
                    }
                }
            }

            override fun onLongClick() {
                state.vibrate()
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })

        time = layout.findViewById(R.id.homeTime)
        time?.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onClick() {
                when (val stateValue = state.getData(DataKey.TIME_CLICK_APP, "default")) {
                    "default" -> {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    "none" -> return
                    else -> {
                        val launchAppIntent =
                            context.packageManager.getLaunchIntentForPackage(stateValue)
                        if (launchAppIntent != null) context.startActivity(launchAppIntent)
                    }
                }
            }

            override fun onLongClick() {
                state.vibrate()
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })

        updateTimeDateStyle()
    }

    private fun updateTimeDateStyle() {
        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)

        date?.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )
        time?.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )
    }

    private fun startTimerToUpdateDateTime() {

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (context as Activity).runOnUiThread {
                    val dateStateValue = state.getData(DataKey.DATE_FORMAT, 1)
                    val timeStateValue = state.getData(DataKey.TIME_FORMAT, 0)

                    date?.text =
                        SimpleDateFormat.getDateInstance(SettingsActivity.mapFormat(dateStateValue))
                            .format(Date())
                    time?.text =
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestureDetector() {
        layout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }

            override fun onLongClick() {
                state.vibrate()
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        })

        val appList = layout.findViewById<RecyclerView>(R.id.homeAppList)
        appList.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onFlingDown() {
                expandNotificationBar()
            }
        })
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
            ContextMenuItem.REORDER -> getHomeAppList()
            ContextMenuItem.RENAME_APP -> getHomeAppList()
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
        val currentApp = homeAppList.find { it.homeOrderIndex == position } ?: return

        val blackTextValue = state.getData(DataKey.BLACK_TEXT, false)

        val imageView: ImageView = holder.itemView.findViewById(R.id.appIcon)
        val textView: TextView = holder.itemView.findViewById(R.id.appName)
        val linearLayout: LinearLayout = holder.itemView.findViewById(R.id.appLayout)

        linearLayout.alpha = if (currentApp.hidden) 0.35f else 1f

        imageView.setImageDrawable(currentApp.icon)
        val stateValue = state.getData(DataKey.ICONS_IN_HOME, true)
        imageView.visibility = if (stateValue) View.VISIBLE else View.GONE

        textView.text = currentApp.name
        textView.setTextColor(
            ContextCompat.getColor(
                context,
                if (blackTextValue) R.color.black else R.color.white
            )
        )

        linearLayout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                val launchAppIntent =
                    context.packageManager.getLaunchIntentForPackage(currentApp.packageName)

                if (launchAppIntent != null) context.startActivity(launchAppIntent)
            }

            override fun onLongClick() {
                state.vibrate()
                if (contextMenuContainer != null) state.showContextMenu(
                    currentApp,
                    contextMenuContainer!!,
                    true,
                    ::onContextMenuClick
                )
            }
        })
    }

    override fun getItemCount(): Int {
        return homeAppList.size
    }
}