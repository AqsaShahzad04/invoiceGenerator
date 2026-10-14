package com.learner.invoicegenerator.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.learner.invoicegenerator.MainActivity
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var hasStartedAnimation = false
    private var splashAnimatorSet: AnimatorSet? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingRunnables = mutableListOf<Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager.getInstance(this)
        val isDarkMode = sessionManager.isDarkModeEnabled() ||
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            binding.splashRootView.setBackgroundColor(Color.parseColor("#121212"))
        } else {
            binding.splashRootView.setBackgroundColor(getColor(R.color.bg_cream))
        }

        setupInitialStates()

        // Guarantee animation triggers after layout pass
        binding.root.post {
            if (!hasStartedAnimation && !isFinishing && !isDestroyed) {
                hasStartedAnimation = true
                startSplashAnimation()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !hasStartedAnimation && ::binding.isInitialized) {
            hasStartedAnimation = true
            startSplashAnimation()
        }
    }

    private fun setupInitialStates() {
        binding.splashCenterGroup.scaleX = 0.5f
        binding.splashCenterGroup.scaleY = 0.5f
        binding.splashCenterGroup.alpha = 0f
        binding.tvSplashTagline.text = "INVOICE"
        binding.tvSplashTagline.translationY = 0f
    }

    private fun startSplashAnimation() {
        binding.splashCenterGroup.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // 1. Initial scale in & fade in of centered icon + "INVOICE"
        val scaleX = ObjectAnimator.ofFloat(binding.splashCenterGroup, "scaleX", 0.5f, 1f).apply {
            duration = 450
            interpolator = OvershootInterpolator(1.2f)
        }
        val scaleY = ObjectAnimator.ofFloat(binding.splashCenterGroup, "scaleY", 0.5f, 1f).apply {
            duration = 450
            interpolator = OvershootInterpolator(1.2f)
        }
        val alpha = ObjectAnimator.ofFloat(binding.splashCenterGroup, "alpha", 0f, 1f).apply {
            duration = 350
        }

        splashAnimatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.splashCenterGroup.setLayerType(View.LAYER_TYPE_NONE, null)
                }
            })
            start()
        }

        // 2. Schedule 1 sec later: "INVOICE" -> "TRACK"
        postScheduled(1000) {
            transitionTextTo("TRACK")
        }

        // 3. Schedule 1 sec later (2000ms total): "TRACK" -> "GET PAID"
        postScheduled(2000) {
            transitionTextTo("GET PAID")
        }

        // 4. Schedule navigation to MainActivity (~2700ms total)
        postScheduled(2700) {
            startMainActivity()
        }
    }

    private fun transitionTextTo(newText: String) {
        if (isFinishing || isDestroyed) return

        val density = resources.displayMetrics.density
        val slideDistance = 22f * density

        // 1. Current word slides upwards and fades out
        val pvhTransYOut = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -slideDistance)
        val pvhAlphaOut = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)

        val slideOut = ObjectAnimator.ofPropertyValuesHolder(binding.tvSplashTagline, pvhTransYOut, pvhAlphaOut).apply {
            duration = 180
            interpolator = FastOutSlowInInterpolator()
        }

        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isFinishing && !isDestroyed) {
                    // Change text to new word
                    binding.tvSplashTagline.text = newText

                    // Set initial position below for new word sliding up into center
                    binding.tvSplashTagline.translationY = slideDistance
                    binding.tvSplashTagline.alpha = 0f

                    // 2. New word slides up into place and fades in
                    val pvhTransYIn = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, slideDistance, 0f)
                    val pvhAlphaIn = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)

                    val slideIn = ObjectAnimator.ofPropertyValuesHolder(binding.tvSplashTagline, pvhTransYIn, pvhAlphaIn).apply {
                        duration = 220
                        interpolator = DecelerateInterpolator()
                    }
                    slideIn.start()
                }
            }
        })

        slideOut.start()
    }

    private fun postScheduled(delayMs: Long, action: () -> Unit) {
        val runnable = Runnable {
            if (!isFinishing && !isDestroyed) {
                action()
            }
        }
        pendingRunnables.add(runnable)
        handler.postDelayed(runnable, delayMs)
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        splashAnimatorSet?.cancel()
        splashAnimatorSet = null
        for (r in pendingRunnables) {
            handler.removeCallbacks(r)
        }
        pendingRunnables.clear()
        super.onDestroy()
    }
}
