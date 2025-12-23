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

    /**
     * Fade in animation with scale
     */
    fun fadeIn(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.alpha = 0f
        view.scaleX = 0.95f
        view.scaleY = 0.95f
        view.visibility = View.VISIBLE

        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
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
     * Fade out animation with scale
     */
    fun fadeOut(view: View, duration: Long = 200, onEnd: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide up animation
     */
    fun slideUp(view: View, duration: Long = 350, onEnd: (() -> Unit)? = null) {
        view.translationY = view.height.toFloat() * 0.3f
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
     * Slide down animation
     */
    fun slideDown(view: View, duration: Long = 250, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat() * 0.3f)
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
     * Bounce animation
     */
    fun bounce(view: View, duration: Long = 400) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    /**
     * Pulse animation - for highlighting
     */
    fun pulse(view: View, scaleTo: Float = 1.1f, duration: Long = 200) {
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
     * Shake animation - for errors
     */
    fun shake(view: View, duration: Long = 500) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = duration
        animator.interpolator = FastOutSlowInInterpolator()
        animator.start()
    }

    /**
     * Rotate animation
     */
    fun rotate(view: View, fromDegrees: Float = 0f, toDegrees: Float = 360f, duration: Long = 500) {
        view.animate()
            .rotation(toDegrees)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                view.rotation = fromDegrees
            }
            .start()
    }

    /**
     * Press animation - like button press
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
                    .setInterpolator(OvershootInterpolator())
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
     * Flip animation
     */
    fun flip(view: View, duration: Long = 400) {
        val animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f)
        animator.duration = duration
        animator.interpolator = FastOutSlowInInterpolator()
        animator.start()
    }

    /**
     * Color change animation (for background)
     */
    fun animateBackgroundColor(view: View, fromColor: Int, toColor: Int, duration: Long = 300) {
        val animator = ValueAnimator.ofArgb(fromColor, toColor)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            view.setBackgroundColor(animation.animatedValue as Int)
        }
        animator.start()
    }

    /**
     * Slide in from right
     */
    fun slideInFromRight(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.translationX = view.width.toFloat()
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
     * Slide out to right
     */
    fun slideOutToRight(view: View, duration: Long = 250, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationX(view.width.toFloat())
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
     * Reveal animation (circular reveal for API 21+)
     */
    fun reveal(view: View, duration: Long = 400) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val cx = view.width / 2
            val cy = view.height / 2
            val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

            val anim = android.view.ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius)
            view.visibility = View.VISIBLE
            anim.duration = duration
            anim.interpolator = FastOutSlowInInterpolator()
            anim.start()
        } else {
            fadeIn(view, duration)
        }
    }

    /**
     * Zoom in animation
     */
    fun zoomIn(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Zoom out animation
     */
    fun zoomOut(view: View, duration: Long = 200, onEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
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