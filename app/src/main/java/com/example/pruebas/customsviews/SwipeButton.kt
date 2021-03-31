package com.example.pruebas.customsviews

import android.animation.*
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.marginBottom
import com.example.pruebas.R
import com.example.pruebas.dp
import com.example.pruebas.textAppearance
import java.util.*


class SwipeButton : RelativeLayout {

    private lateinit var slidingButton: ImageView
    private var initialX = 0f
    private var active = false
    private var initialButtonWidth = 0
    private var centerText: AppCompatTextView? = null

    private var disabledDrawable: Drawable? = null
    private var enabledDrawable: Drawable? = null
    private var onClickListener:OnClickListener? = null


    constructor(context: Context) : this(context, null){
        init(context,null,-1,-1)
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init(context,attrs,-1,-1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context,attrs,defStyleAttr,-1)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes){
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {

        context.withStyledAttributes(set = attrs, attrs = R.styleable.SwipeButton) {
            val textTitleStyleAttribute: Int = this.getResourceId(R.styleable.SwipeButton_title_text_style, 0)

            val titleString = if(this.getString(R.styleable.SwipeButton_title).isNullOrEmpty()) "Button"
            else this.getString(R.styleable.SwipeButton_title)

            val textAllCaps = this.getBoolean(R.styleable.SwipeButton_android_textAllCaps,true)

            val drawableIcon = this.getDrawable(R.styleable.SwipeButton_icon_to_slide)

            val backgroundColor = this.getColor(R.styleable.SwipeButton_button_background_color,0)

            val swipeButtonBackgroundColor = this.getColor(R.styleable.SwipeButton_swipe_button_background_color,0)



            val background = RelativeLayout(context)

            val layoutParamsView = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    45.dp
            )

            layoutParamsView.addRule(CENTER_IN_PARENT, TRUE)
            background.background =  ContextCompat.getDrawable(context, R.drawable.shape_rounded)
            if(backgroundColor != 0){
                background.background.setTint(backgroundColor)
            }
            addView(background, layoutParamsView)

            val centerText = AppCompatTextView(context)

            this@SwipeButton.centerText = centerText

            val layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )

            layoutParams.addRule(CENTER_IN_PARENT, TRUE)
            centerText.text = if(textAllCaps) titleString?.toUpperCase(Locale.ROOT) else titleString

            if(textTitleStyleAttribute != 0){
                centerText.textAppearance(textTitleStyleAttribute)
            }else{
                centerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                centerText.setTextColor(Color.WHITE)
            }
            this@SwipeButton.centerText?.setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            background.addView(centerText, layoutParams)

            //add moving icon
            val swipeButton = ImageView(context)
            slidingButton = swipeButton

            if(drawableIcon != null){
                disabledDrawable = drawableIcon
                enabledDrawable = drawableIcon
            }else{
                disabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ekt_arrow)
                enabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ekt_arrow)
            }


            slidingButton.setImageDrawable(disabledDrawable)
            slidingButton.setPadding(20.dp, 10.dp, 20.dp, 10.dp)

            val layoutParamsButton = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)

            layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
            layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
            swipeButton.background = ContextCompat.getDrawable(context, R.drawable.shape_button)

            if(swipeButtonBackgroundColor != 0){
                swipeButton.background.setTint(swipeButtonBackgroundColor)
            }
            swipeButton.setImageDrawable(disabledDrawable)
            background.addView(swipeButton, layoutParamsButton)

            setOnTouchListener(getButtonTouchListener())
        }
    }

    fun setTextStyleText(style: Int){
        centerText = AppCompatTextView(context)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getButtonTouchListener(): OnTouchListener? {
        return OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->{return@OnTouchListener true}
                MotionEvent.ACTION_MOVE ->  {
                    //Movement logic here
                    if (initialX == 0F) {
                        initialX = slidingButton.x
                    }
                    if (event.x > initialX + slidingButton.width / 2 &&
                            event.x + slidingButton.width / 2 < width) {
                        slidingButton.x = event.x - slidingButton.width / 2
                        centerText!!.alpha = 1 - 1.3f * (slidingButton.x + slidingButton.width) / width
                    }

                    if  (event.x + slidingButton.width / 2 > width &&
                            slidingButton.x + slidingButton.width / 2 < width) {
                        slidingButton.x = (width - slidingButton.width).toFloat()
                    }

                    if  (event.x < slidingButton.width / 2 &&
                            slidingButton.x > 0) {
                        slidingButton.x = 0F
                    }
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    //Release logic here
                    initialX = 0f
                    if (active) {
                        collapseButton()
                    } else {
                        initialButtonWidth = slidingButton.width

                        if (slidingButton.x + slidingButton.width > width * 0.95) {
                            expandButton()
                            onClickListener?.onClick(this)
                        } else {
                            moveButtonBack()
                        }
                    }
                    return@OnTouchListener true
                }
            }
            false
        }
    }

    private fun expandButton() {
        val positionAnimator = ValueAnimator.ofFloat(this.slidingButton.x, 0f)
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            this.slidingButton.x = x
        }
        val widthAnimator = ValueAnimator.ofInt(
                this.slidingButton.width,
                width)
        widthAnimator.addUpdateListener {
            val params = this.slidingButton.layoutParams
            params.width = (widthAnimator.animatedValue as Int)
            this.slidingButton.layoutParams = params
        }
        val animatorSet = AnimatorSet()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                active = true
                slidingButton.setImageDrawable(enabledDrawable)
            }
        })
        animatorSet.playTogether(positionAnimator, widthAnimator)
        animatorSet.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun collapseButton() {
        val widthAnimator = ValueAnimator.ofInt(
                slidingButton.width,
                initialButtonWidth)
        widthAnimator.addUpdateListener {
            val params = slidingButton.layoutParams
            params.width = (widthAnimator.animatedValue as Int)
            slidingButton.layoutParams = params
        }
        widthAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                active = false
                slidingButton.setImageDrawable(disabledDrawable)
            }
        })
        val objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, widthAnimator)
        animatorSet.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun moveButtonBack() {
        val positionAnimator = ValueAnimator.ofFloat(slidingButton.x, 0f)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            slidingButton.x = x
        }
        val objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1f)
        positionAnimator.duration = 200
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, positionAnimator)
        animatorSet.start()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        //super.setOnClickListener(l)
        onClickListener = l
    }

}