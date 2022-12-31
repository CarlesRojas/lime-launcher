package app.pinya.lime

import OnSwipeTouchListener
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class ItemAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class HomeAdapter(context: Context, state: State, layout: ViewGroup) :
    RecyclerView.Adapter<ItemAppViewHolder>() {

    private val context: Context
    private val state: State
    private val layout: ViewGroup
    private val date: TextView
    private val time: TextView

    private var timer: Timer? = Timer()

    init {
        this.context = context
        this.state = state
        this.layout = layout

        date = layout.findViewById(R.id.homeDate)
        time = layout.findViewById(R.id.homeTime)

        startTimerToUpdateDateTime()
        initGestureDetector()
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


    var showNotificationsJob: Job? = null

    private fun initGestureDetector() {
        layout.setOnLongClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
            true
        }

        layout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeBottom() {
                expandNotificationBar()
            }
        })

        val appList = layout.findViewById<RecyclerView>(R.id.homeAppList)

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

            if (launchAppIntent != null) context.startActivity(launchAppIntent)
        }
    }

    override fun getItemCount(): Int {
        return this.state.getInstalledAppList().size
    }
}