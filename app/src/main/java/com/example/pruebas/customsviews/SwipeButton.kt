package com.example.pruebas.customsviews

import android.animation.*
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.pruebas.R


class SwipeButton : RelativeLayout {

    private lateinit var slidingButton: ImageView
    private var initialX = 0f
    private var active = false
    private var initialButtonWidth = 0
    private var centerText: TextView? = null

    private var disabledDrawable: Drawable? = null
    private var enabledDrawable: Drawable? = null


    constructor(context: Context?) : this(context, null){
        init(context,null,-1,-1)
    }
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0){
        init(context,attrs,-1,-1)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context,attrs,defStyleAttr,-1)
    }

    @TargetApi(21)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes){
        init(context, attrs, defStyleAttr, defStyleRes)
    }
    private fun init(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val background = RelativeLayout(context)

        val layoutParamsView = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutParamsView.addRule(CENTER_IN_PARENT, TRUE)

        background.background = ContextCompat.getDrawable(context!!, R.drawable.shape_rounded)

        addView(background, layoutParamsView)

        val centerText = TextView(context)
        this.centerText = centerText

        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutParams.addRule(CENTER_IN_PARENT, TRUE)
        centerText.text = "Desliza para terminar" //add any text you need

        centerText.setTextColor(Color.WHITE)
        centerText.setPadding(35, 35, 35, 35)
        background.addView(centerText, layoutParams)

        //add moving icon
        val swipeButton = ImageView(context)
        slidingButton = swipeButton

        disabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ekt_arrow)
        enabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ekt_arrow)

        slidingButton.setImageDrawable(disabledDrawable)
        slidingButton.setPadding(40, 30, 40, 30)

        val layoutParamsButton = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
        layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
        swipeButton.background = ContextCompat.getDrawable(context, R.drawable.shape_button)
        swipeButton.setImageDrawable(disabledDrawable)
        addView(swipeButton, layoutParamsButton)

        setOnTouchListener(getButtonTouchListener())

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
                    if (active) {
                        collapseButton()
                    } else {
                        initialButtonWidth = slidingButton.width

                        if (slidingButton.x + slidingButton.width > width * 0.85) {
                            expandButton()
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
}