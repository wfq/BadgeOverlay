package com.wfq.badgeoverlay

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import java.lang.ref.WeakReference
import kotlin.math.pow


/**
 * @author  wfq
 * @date    2020/6/7
 */
class BadgeDrawable(context: Context) : Drawable() {

    private val contextRef: WeakReference<Context> = WeakReference(context)
    private var anchorViewRef: WeakReference<View>? = null

    // 圆角
    private val outerR = FloatArray(8)
    private val shapeDrawable: ShapeDrawable

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val badgeBounds: Rect

    private val badgeRadius: Float
    private var badgeWithTextRadius: Float
    private val badgeWidePadding: Float

    internal val badgeState: BadgeState

    private var badgeCenterX = 0f
    private var badgeCenterY = 0f
    private var maxBadgeNumber = 999
    private var halfBadgeWidth = 0f
    private var halfBadgeHeight = 0f

    private var cornerRadius = 0f
    // todo 单独设置圆角
//    var topLeftCorner = 0f
//    var topRightCorner = 0f
//    var bottomRightCorner = 0f
//    var bottomLeftCorner = 0f

    init {
        badgeState = BadgeState(context)
        badgeBounds = Rect()
        val shape = RoundRectShape(outerR, null, null)
        shapeDrawable = ShapeDrawable(shape)

        val res = context.resources
        badgeRadius = res.getDimensionPixelSize(R.dimen.wfq_bo_badge_radius).toFloat()
        badgeWithTextRadius =
            res.getDimensionPixelSize(R.dimen.wfq_bo_badge_with_text_radius).toFloat()
        badgeWidePadding =
            res.getDimensionPixelSize(R.dimen.wfq_badge_long_text_horizontal_padding).toFloat()

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = badgeState.textSize.toFloat()
        setTextColor(badgeState.textColor)
    }


    fun updateBadgeCoordinates(anchorView: View) {
        anchorViewRef = WeakReference(anchorView)
        updateCenterAndBounds()
        invalidateSelf()
    }

    private fun updateCenterAndBounds() {
        val context = contextRef.get()
        val anchorView = anchorViewRef?.get()
        if (context == null || anchorView == null) return

        val tmpRect = Rect()
        tmpRect.set(badgeBounds)

        val anchorRect = Rect()
        anchorView.getDrawingRect(anchorRect)

        calculateCenterAndBounds(context, anchorRect, anchorView)

        updateBadgeBounds()

        if (tmpRect != badgeBounds) {
            shapeDrawable.bounds = badgeBounds
        }
    }

    private fun calculateCenterAndBounds(context: Context, anchorRect: Rect, anchorView: View) {
        badgeCenterY = when (badgeState.gravity) {
            BOTTOM_END, BOTTOM_START -> (anchorRect.bottom - badgeState.verticalOffset).toFloat()
            else -> (anchorRect.top + badgeState.verticalOffset).toFloat()
        }

        if (hasText() || getNumber() > MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
            cornerRadius = badgeWithTextRadius
            halfBadgeHeight = cornerRadius
            val badgeText = getBadgeText()
            halfBadgeWidth =
                textPaint.measureText(badgeText, 0, badgeText.length) / 2f + badgeWidePadding
        } else {
            cornerRadius = if (hasNumber()) badgeWithTextRadius else badgeRadius
            halfBadgeHeight = cornerRadius
            halfBadgeWidth = cornerRadius
        }

        badgeCenterX = when (badgeState.gravity) {
            BOTTOM_START, TOP_START -> {
                if (ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    (anchorRect.left + badgeState.horizontalOffset).toFloat()
                } else {
                    (anchorRect.right - badgeState.horizontalOffset).toFloat()
                }
            }
            else -> {
                if (ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    (anchorRect.right - badgeState.horizontalOffset).toFloat()
                } else {
                    (anchorRect.left + badgeState.horizontalOffset).toFloat()
                }
            }
        }
    }

    fun hasNumber(): Boolean {
        return badgeState.number != BADGE_NUMBER_NONE
    }

    fun getNumber(): Int {
        if (!hasNumber()) {
            return 0
        }
        return badgeState.number
    }

    fun setText(num: Int) {
        val number = num.coerceAtLeast(0)
        if (badgeState.number != number) {
            badgeState.number = number
            badgeState.text = null
            updateCenterAndBounds()
            invalidateSelf()
        }
    }

    fun hasText(): Boolean {
        return badgeState.text?.isNotEmpty() ?: false
    }

    fun getText(): String {
        if (!hasText()) {
            return ""
        }
        return badgeState.text!!
    }

    fun setText(text: String?) {
        if (badgeState.text != text) {
            badgeState.text = text
            badgeState.number = 0
            updateCenterAndBounds()
            invalidateSelf()
        }
    }

    fun getBadgeText(): String {
        return when {
            hasText() -> {
                getText()
            }
            hasNumber() -> {
                if (getNumber() <= maxBadgeNumber) {
                    getNumber().toString()
                } else {
                    val context = contextRef.get() ?: return ""
                    context.getString(
                        R.string.wfq_exceed_max_badge_number_suffix,
                        maxBadgeNumber,
                        "+"
                    )
                }
            }
            else -> {
                ""
            }
        }
    }

    fun setGravity(gravity: Int) {
        if (badgeState.gravity != gravity) {
            badgeState.gravity = gravity
            updateCenterAndBounds()
        }
    }

    fun setTextColor(@ColorInt badgeTextColor: Int) {
        badgeState.textColor = badgeTextColor
        if (textPaint.color != badgeTextColor) {
            textPaint.color = badgeTextColor
            invalidateSelf()
        }
    }

    fun setTextSize(badgeTextSize: Int) {
        if (badgeState.textSize != badgeTextSize) {
            badgeState.textSize = badgeTextSize
            textPaint.textSize = badgeTextSize.toFloat()
            invalidateSelf()
        }
    }

    fun setHorizontalOffset(px: Int) {
        if (badgeState.horizontalOffset != px) {
            badgeState.horizontalOffset = px
            updateCenterAndBounds()
        }
    }

    fun setVerticalOffset(px: Int) {
        if (badgeState.verticalOffset != px) {
            badgeState.verticalOffset = px
            updateCenterAndBounds()
        }
    }

    fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        badgeState.backgroundColor = backgroundColor
    }

    fun setMaxCharacterCount(maxCharacterCount: Int) {
        if (badgeState.maxCharacterCount != maxCharacterCount) {
            badgeState.maxCharacterCount = maxCharacterCount
            updateMaxBadgeNumber()
            updateCenterAndBounds()
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        shapeDrawable.paint.color = badgeState.backgroundColor
        shapeDrawable.draw(canvas)
        // draw text
        val badgeText = getBadgeText()
        if (badgeText.isNotEmpty()) {
            val textBounds = Rect()
            textPaint.getTextBounds(badgeText, 0, badgeText.length, textBounds)
            val fontMetrics = textPaint.fontMetrics
            canvas.drawText(
                badgeText,
                badgeCenterX,
                badgeCenterY + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom,
                textPaint
            )
        }
    }

    override fun setAlpha(alpha: Int) {
        badgeState.alpha = alpha
        textPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("Not yet implemented")
    }

    override fun getIntrinsicHeight(): Int {
        return badgeBounds.height()
    }

    override fun getIntrinsicWidth(): Int {
        return badgeBounds.width()
    }

    private fun updateMaxBadgeNumber() {
        maxBadgeNumber =
            10.0.pow(badgeState.maxCharacterCount.toDouble() - 1).toInt() - 1
    }

    private fun updateBadgeBounds() {
        for (index in outerR.indices) {
            outerR[index] = cornerRadius
        }
        badgeBounds.set(
            (badgeCenterX - halfBadgeWidth).toInt(),
            (badgeCenterY - halfBadgeHeight).toInt(),
            (badgeCenterX + halfBadgeWidth).toInt(),
            (badgeCenterY + halfBadgeHeight).toInt()
        )
    }

    class BadgeState(context: Context) {

        var alpha = 255
        var gravity = TOP_END

        // 角标数字
        var number = BADGE_NUMBER_NONE

        // 角标文本
        var text: String? = null

        var backgroundColor: Int =
            ResourcesCompat.getColor(Resources.getSystem(), android.R.color.holo_red_dark, null)

        var textColor: Int =
            ResourcesCompat.getColor(Resources.getSystem(), android.R.color.white, null)

        @Dimension(unit = Dimension.PX)
        var textSize: Int = context.resources.getDimensionPixelSize(R.dimen.wfq_badge_text_size)

        @Dimension(unit = Dimension.PX)
        var horizontalOffset = 0

        @Dimension(unit = Dimension.PX)
        var verticalOffset = 0

        // 数字模式下，角标能显示的字符长度
        var maxCharacterCount = DEFAULT_MAX_BADGE_CHARACTER_COUNT
    }

    companion object {

        /** The badge is positioned along the top and end edges of its anchor view  */
        const val TOP_END = Gravity.TOP or Gravity.END

        /** The badge is positioned along the top and start edges of its anchor view  */
        const val TOP_START = Gravity.TOP or Gravity.START

        /** The badge is positioned along the bottom and end edges of its anchor view  */
        const val BOTTOM_END = Gravity.BOTTOM or Gravity.END

        /** The badge is positioned along the bottom and start edges of its anchor view  */
        const val BOTTOM_START = Gravity.BOTTOM or Gravity.START

        private const val DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4

        private const val MAX_CIRCULAR_BADGE_NUMBER_COUNT = 9

        private const val BADGE_NUMBER_NONE = -1

        private const val CORNER_RADIUS_ZERO = -1

        /** Creates an instance of BadgeDrawable with default values.  */
//        fun create(context: Context): BadgeDrawable {
//            return createFromAttributes(
//                context,  /* attrs= */
//                null,
//                DEFAULT_THEME_ATTR,
//                StyleContext.DEFAULT_STYLE
//            )
//        }
//
//        fun createFromResource(context: Context, @XmlRes id: Int): BadgeDrawable {
//            val attrs: AttributeSet = DrawableUtils.parseDrawableXml(context, id, "badge")
//            @StyleRes var style: Int = attrs.styleAttribute
//            if (style == 0) {
//                style = DEFAULT_STYLE
//            }
//            return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, style)
//        }
//
//        private fun createFromAttributes(
//            context: Context,
//            attrs: AttributeSet,
//            @AttrRes defStyleAttr: Int,
//            @StyleRes defStyleRes: Int
//        ): BadgeDrawable {
//            val badge = BadgeDrawable(context)
//            badge.loadDefaultStateFromAttributes(context, attrs, defStyleAttr, defStyleRes)
//            return badge
//        }
    }
}