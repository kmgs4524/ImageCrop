package tw.com.program.crop

import androidx.annotation.IntDef
import androidx.annotation.IntRange

@IntDef(CIRCLE, RECTANGLE)
annotation class CropShape

/**
 * 圓形才切框
 */
const val CIRCLE = 1

/**
 * 矩形才切框
 */
const val RECTANGLE = 2