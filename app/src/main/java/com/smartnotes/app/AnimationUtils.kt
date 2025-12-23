package com.smartnotes.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object AnimationUtils {

    // Optimized durations
    private const val DURATION_FAST = 150L
    private const val DURATION_NORMAL = 250L
    private const val DURATION_SLOW = 350L

    /**
     * Fade in animation - Optimized
     */
    fun fadeIn(view: View, duration: Long = DURATION_NORMAL, onEnd: (() -> Unit)? = null) {
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Fade out animation - Optimized
     */
    fun fadeOut(view: View, duration: Long = DURATION_FAST, onEnd: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.alpha = 1f
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide up animation - Optimized
     */
    fun slideUp(view: View, duration: Long = DURATION_NORMAL, onEnd: (() -> Unit)? = null) {
        view.translationY = view.height.toFloat() * 0.2f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide down animation - Optimized
     */
    fun slideDown(view: View, duration: Long = DURATION_NORMAL, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat() * 0.2f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.translationY = 0f
                    view.alpha = 1f
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Bounce animation - Simplified and optimized
     */
    fun bounce(view: View, duration: Long = DURATION_NORMAL) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator(1.5f))
            .start()
    }

    /**
     * Pulse animation - Optimized with smaller scale
     */
    fun pulse(view: View, scaleTo: Float = 1.08f, duration: Long = DURATION_FAST) {
        view.animate()
            .scaleX(scaleTo)
            .scaleY(scaleTo)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
            }
            .start()
    }

    /**
     * Shake animation - Simplified
     */
    fun shake(view: View, duration: Long = 400) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f)
        animator.duration = duration
        animator.interpolator = FastOutSlowInInterpolator()
        animator.start()
    }

    /**
     * Press animation - Optimized
     */
    fun pressAnimation(view: View, onEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator(1.2f))
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            onEnd?.invoke()
                        }
                    })
                    .start()
            }
            .start()
    }

    /**
     * Color change animation - Optimized
     */
    fun animateBackgroundColor(view: View, fromColor: Int, toColor: Int, duration: Long = DURATION_NORMAL) {
        val animator = ValueAnimator.ofArgb(fromColor, toColor)
        animator.duration = duration
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { animation ->
            view.setBackgroundColor(animation.animatedValue as Int)
        }
        animator.start()
    }

    /**
     * Slide in from right - Optimized
     */
    fun slideInFromRight(view: View, duration: Long = DURATION_NORMAL, onEnd: (() -> Unit)? = null) {
        view.translationX = view.width.toFloat() * 0.3f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide out to right - Optimized
     */
    fun slideOutToRight(view: View, duration: Long = DURATION_FAST, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationX(view.width.toFloat() * 0.3f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.translationX = 0f
                    view.alpha = 1f
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Zoom in animation - Simplified
     */
    fun zoomIn(view: View, duration: Long = DURATION_NORMAL, onEnd: (() -> Unit)? = null) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator(1.5f))
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Zoom out animation - Simplified
     */
    fun zoomOut(view: View, duration: Long = DURATION_FAST, onEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.alpha = 1f
                    onEnd?.invoke()
                }
            })
            .start()
    }
}