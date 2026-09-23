import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class StableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.rgb(16, 17, 22))

        alpha = 1f
        visibility = VISIBLE

        overScrollMode = OVER_SCROLL_NEVER

        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY

        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        isFocusable = true
        isFocusableInTouchMode = true

        clipToOutline = false
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(16, 17, 22))
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(16, 17, 22))
        super.dispatchDraw(canvas)
    }

    override fun onVisibilityChanged(
        changedView: android.view.View,
        visibility: Int
    ) {
        super.onVisibilityChanged(changedView, VISIBLE)

        if (visibility != VISIBLE) {
            post {
                alpha = 1f
                this.visibility = VISIBLE
                invalidate()
            }
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(VISIBLE)

        if (visibility != VISIBLE) {
            post {
                alpha = 1f
                this.visibility = VISIBLE
                invalidate()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                invalidate()
            }
        }

        return super.onTouchEvent(event)
    }
}