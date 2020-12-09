package tw.com.program.imagecrop.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BarStyleParameters (

    /**
     * Toolbar 背景艷色的 resource id
     */
    val barBackgroundColor: Int,

    /**
     * 返回的 drawable id
     */
    val backDrawable: Int,


    val titleText: String,

    /**
     * 標題文字顏色的 resource id
     */
    val titleTextColor: Int,

    /**
     * 裁切按鈕的 resource id
     */
    val saveTextColor: Int
) : Parcelable