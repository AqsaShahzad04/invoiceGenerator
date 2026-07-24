package com.learner.invoicegenerator

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding?=null
    private val binding get()=_binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding= ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val navOptions = navOptions {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }

        val bottomNavBar = findViewById<View>(R.id.bottomNavBar)

        val fabbtn = findViewById<View>(R.id.fab)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeScreenFragment || destination.id == R.id.invoicesFragment || destination.id == R.id.clientsFragment || destination.id == R.id.settingsFragment) {
                bottomNavBar.visibility = View.VISIBLE
                fabbtn.visibility = View.VISIBLE
            } else {
                bottomNavBar.visibility = View.GONE
                fabbtn.visibility = View.GONE
            }
            updateSelectedTab(destination.id)
        }
        binding.navHome.setOnClickListener {
            navController.navigate(R.id.homeScreenFragment, null, navOptions)
        }

      binding.navInvoices.setOnClickListener {
          navController.navigate(R.id.invoicesFragment, null, navOptions)
      }
      binding.navClients.setOnClickListener {
          navController.navigate(R.id.clientsFragment, null, navOptions)
      }
        binding.navSettings.setOnClickListener {
            navController.navigate(R.id.settingsFragment, null, navOptions)

        }
    }
    private fun updateSelectedTab(selectedId: Int) {
        val tabs = mapOf(
            R.id.homeScreenFragment to Triple(binding.iconHome, binding.labelHome, binding.indicatorHome),
            R.id.invoicesFragment to Triple(binding.iconInvoices, binding.labelInvoices, binding.indicatorInvoices),
            R.id.clientsFragment to Triple(binding.iconClients, binding.labelClients, binding.indicatorClients),
            R.id.settingsFragment to Triple(binding.iconSettings, binding.labelSettings, binding.indicatorSettings)
        )

        for ((fragmentId, views) in tabs) {
            val (icon, label, indicator) = views
            if (fragmentId == selectedId) {
                icon.setColorFilter(getColor(R.color.primary_green))
                label.setTextColor(getColor(R.color.primary_green))
                indicator.setBackgroundResource(R.drawable.bg_top_indicator)
            } else {
                icon.setColorFilter(getColor(R.color.subheading_color_home))
                label.setTextColor(getColor(R.color.subheading_color_home))
                indicator.setBackgroundColor(getColor(android.R.color.transparent))
            }
        }
    }
}