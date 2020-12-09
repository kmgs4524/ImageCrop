package tw.com.program.imagecrop.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * 透明 Status Bar Activity
 */
open class TransparentBarActivity : AppCompatActivity() {

    protected lateinit var barStyleParameters: BarStyleParameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransparentStatusBar()
    }

    private fun setTransparentStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) // 取消半透明
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，layout 上方會被 status bar 覆蓋
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 當前 UI 是 Light Style，icon 變成深色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 自行設置 status bar 背景色
        window.statusBarColor = Color.TRANSPARENT // 設置 status bar 背景為透明色
    }

    /**
     * 全螢幕顯示後 toolbar 會被 status bar 的圖示擋住，故增加 toolBar 的上內距
     * @notice 這個方法要在 setContentView 後執行
     */
    protected fun setToolbarPadding(toolbar: Toolbar) {
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    private fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")

        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }
}