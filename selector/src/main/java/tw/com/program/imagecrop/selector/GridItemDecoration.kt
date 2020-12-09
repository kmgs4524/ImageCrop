package tw.com.program.imagecrop.selector

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(lineColor: Int, lineWidth: Int) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth.toFloat()
        color = lineColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val childViews = parent.children
        childViews.forEach {
            val childPosX = it.x
            val childPosY = it.y

            // draw right line
            c.drawLine(
                childPosX + it.width,
                childPosY,
                childPosX + it.width,
                childPosY + it.height,
                paint
            )
            // draw bottom line
            c.drawLine(
                childPosX,
                childPosY + it.height,
                childPosX + it.width,
                childPosY + it.height,
                paint
            )
        }
    }
}