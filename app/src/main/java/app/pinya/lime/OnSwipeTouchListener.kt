import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet.Motion
import kotlin.math.abs

private const val SWIPE_THRESHOLD: Int = 100
private const val SWIPE_VELOCITY_THRESHOLD: Int = 100

internal open class OnSwipeTouchListener(c: Context) :
    OnTouchListener {
    private val gestureDetector: GestureDetector

    private val context: Context

    var initialY = 0f
    var initialX = 0f
    var swipeCompleted = false

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        onAnyTouch()

        /*
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            swipeCompleted = false
            initialY = motionEvent.y
            initialX = motionEvent.x
        }

        val yDisp = initialY - motionEvent.y
        val completed = swipeCompleted

        if (!swipeCompleted && yDisp > 100) {
            swipeCompleted = true
            Toast.makeText(context, "Swipe down gesture detected", Toast.LENGTH_SHORT)
                .show()
        } else if (!swipeCompleted && yDisp < 100) {
            swipeCompleted = true
            Toast.makeText(context, "Swipe up gesture detected", Toast.LENGTH_SHORT)
                .show()
        }*/


        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            if (e1 == null || e2 == null) return true
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {

                    if (abs(diffX) > SWIPE_THRESHOLD && abs(
                            velocityX
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) onFlingRight()
                        else onFlingLeft()
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(
                            velocityY
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffY < 0) onFlingUp()
                        else onFlingDown()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return false
        }
    }

    open fun onAnyTouch() {}
    open fun onFlingRight() {}
    open fun onFlingLeft() {}
    open fun onFlingUp() {}
    open fun onFlingDown() {}
    private fun onClick() {}
    private fun onDoubleClick() {}
    private fun onLongClick() {}

    init {
        context = c
        gestureDetector = GestureDetector(c, GestureListener())
    }
}
