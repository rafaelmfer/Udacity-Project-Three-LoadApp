package com.udacity.loadapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val attributesTypedArray: TypedArray

    private lateinit var paint: Paint

    private val pointPosition: PointF = PointF(0.0f, 0.0f)
    private val textRect = Rect()

    private var widthSize = 0
    private var heightSize = 0

    private var currentWidth = 0

    private var text = ""
    private var buttonTextColor: Int = 0
    private var buttonBackground: Int = 0

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                valueAnimator.cancel()
            }
            ButtonState.Loading -> {
                valueAnimator.repeatMode = ValueAnimator.RESTART
                valueAnimator.repeatCount = ValueAnimator.INFINITE
                valueAnimator.duration = 1000L
                valueAnimator.setValues(
                    PropertyValuesHolder.ofInt("rect", 0, widthSize)
                )
                valueAnimator.addUpdateListener {
                    currentWidth = it.getAnimatedValue("rect") as Int
                    invalidate()
                }
                valueAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        isEnabled = false
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        isEnabled = true
                    }
                })
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
            }
        }
    }

    init {
        isClickable = true
        valueAnimator.setFloatValues(0f, 1f)
        attributesTypedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, defStyleAttr, 0).apply {
            buttonTextColor = getInt(R.styleable.LoadingButton_android_textColor, 0)
            buttonBackground = getInt(R.styleable.LoadingButton_android_backgroundTint, 0)

            recycle()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawBackgroundButton(canvas)
        when (buttonState) {
            ButtonState.Clicked -> {
                text = context.getString(R.string.button_name)
                drawTextButton(canvas)
            }
            is ButtonState.Loading -> {
                text = context.getString(R.string.button_loading)
                drawTextButton(canvas)
                drawRectLoading(canvas)
                drawArcLoading(canvas)
            }
            ButtonState.Completed -> {
                text = context.getString(R.string.button_name)
                drawTextButton(canvas)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun drawTextButton(canvas: Canvas?) {
        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = context.resources.getDimension(R.dimen.default_text_size)
            color = buttonTextColor
        }
        paint.getTextBounds(text, 0, text.length, textRect)
        pointPosition.computeXYForText(textRect)
        canvas?.drawText(text, pointPosition.x, pointPosition.y, paint)
    }

    private fun PointF.computeXYForText(textRect: Rect) {
        x = widthSize.toFloat() / 2
        y = heightSize.toFloat() / 2 - textRect.centerY()
    }

    private fun drawRectLoading(canvas: Canvas?) {
        canvas?.drawRect(
            0f,
            0f,
            currentWidth.toFloat(),
            heightSize.toFloat(),
            paint
        )
    }

    private fun drawArcLoading(canvas: Canvas?) {
        paint.color = ContextCompat.getColor(context, R.color.yellow_400)

        val rectF = RectF()
        val circleDiameter = 60.0f
        val circleSize = heightSize - paddingBottom - circleDiameter
        rectF.set(circleDiameter, circleDiameter, circleSize, circleSize)

        canvas?.drawArc(
            rectF,
            0F,
            360F,
            true,
            paint
        )
    }

    private fun drawBackgroundButton(canvas: Canvas?) {
        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = buttonBackground
        }
        canvas?.drawColor(paint.color)
    }

    fun setState(state: ButtonState) {
        buttonState = state
    }
}