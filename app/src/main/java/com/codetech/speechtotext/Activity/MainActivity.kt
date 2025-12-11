package com.codetech.speechtotext.Activity

import CustomNavigationAdapter
import android.annotation.SuppressLint

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import com.codetech.speechtotext.R
import com.codetech.speechtotext.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import android.content.Intent
import com.codetech.speechtotext.Dialog.showExitAppDialog
import com.codetech.speechtotext.Dialog.showRateUsDialog
import com.codetech.speechtotext.models.StrokeManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navigationAdapter: CustomNavigationAdapter
    private var shouldShowBottomNav = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.main
        navigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(this)





        setupClickListener()
        setupReselectedNavigationItem()
        setupNavigationDrawer()
        setupBottomNavigation()
        setupKeyboardVisibilityListener()
        StrokeManager.initLanguageModel()
    }

    private fun setupClickListener() {
        binding.customToolbar.rightIcon.setOnClickListener {
            navController.navigate(R.id.premiumFragment)
        }

        binding.customToolbar.leftIcon.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }

    }

    private fun setupReselectedNavigationItem() {
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.speechToTextFragment -> {}
                R.id.textToTranslateFragment -> {}
                R.id.dictionaryFragment -> {}
                R.id.OCRFragment -> {}
            }
        }
    }


    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        binding.run {
            shouldShowBottomNav = when (destination.id) {
                R.id.speechToTextFragment,
                R.id.textToTranslateFragment,
                R.id.dictionaryFragment,
                R.id.OCRFragment -> true

                R.id.historyFragment,
                R.id.premiumFragment,
                R.id.OCRToTextTranslateFragment,
                R.id.scanOCRFragment,
                R.id.photoFragment,
                R.id.rateUs,
                R.id.languageFragment,
                R.id.favouriteFragment,
                R.id.splashFragment,
                R.id.onboardingFragment,
                R.id.textDrawTranslateFragment -> false

                else -> true
            }

            updateBottomNavVisibility()

            when (destination.id) {
                R.id.speechToTextFragment -> {
                    customToolbar.toolbarTitle.text = "Speech to Text"
                    binding.customToolbar.toolbar.visibility = View.VISIBLE
                }

                R.id.textToTranslateFragment -> {
                    customToolbar.toolbarTitle.text = "Translate"
                    binding.customToolbar.toolbar.visibility = View.VISIBLE

                }

                R.id.dictionaryFragment -> {
                    customToolbar.toolbarTitle.text = "Dictionary"
                    binding.customToolbar.toolbar.visibility = View.VISIBLE
                }

                R.id.OCRFragment -> {
                    customToolbar.toolbar.visibility = View.GONE
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(this)
        window.decorView.rootView.viewTreeObserver.removeOnGlobalLayoutListener(null)
    }


    @SuppressLint("InflateParams")
    private fun setupNavigationDrawer() {
        val navigationDrawerLayout = layoutInflater.inflate(R.layout.nav_drawer_layout, null)
        binding.navView.addView(navigationDrawerLayout)

        val recyclerView = navigationDrawerLayout.findViewById<RecyclerView>(R.id.nav_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val navigationItems = listOf(
            CustomNavigationAdapter.NavigationItem(
                R.id.languageFragment,
                getString(R.string.languages),
                R.drawable.ic_language
            ),
            CustomNavigationAdapter.NavigationItem(
                R.id.favouriteFragment,
                getString(R.string.favorite_title),
                R.drawable.ic_fav
            ),
            CustomNavigationAdapter.NavigationItem(
                R.id.historyFragment,
                getString(R.string.history),
                R.drawable.ic_history
            ),
            CustomNavigationAdapter.NavigationItem(
                R.id.rateUs,
                getString(R.string.rate_us),
                R.drawable.ic_rate
            ),
            CustomNavigationAdapter.NavigationItem(
                R.id.share,
                getString(R.string.share),
                R.drawable.ic_share
            ),
            CustomNavigationAdapter.NavigationItem(
                R.id.exit,
                getString(R.string.exit),
                R.drawable.ic_exit
            )
        )

        navigationAdapter = CustomNavigationAdapter(this, navigationItems) { item ->
            when (item.id) {
                R.id.languageFragment -> {
                    navController.navigate(R.id.languageFragment)
                    binding.customToolbar.toolbar.visibility = View.GONE
                }

                R.id.favouriteFragment -> {
                    navController.navigate(R.id.favouriteFragment)
                    binding.customToolbar.toolbar.visibility = View.GONE
                }

                R.id.historyFragment -> {
                    navController.navigate(R.id.historyFragment)
                }

                R.id.rateUs -> {
                    showRateUsDialog(this)
//                    navController.navigate(R.id.rateUs)
//                    binding.customToolbar.toolbar.visibility = View.GONE
                }

                R.id.share -> {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out this amazing app: https://play.google.com/store/apps/details?id=${packageName}")
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                }

                R.id.exit -> {
                    showExitAppDialog(this)
                }
            }
            binding.main.closeDrawer(GravityCompat.START)
        }

        recyclerView.adapter = navigationAdapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.speechToTextFragment -> {
                        navController.navigate(R.id.speechToTextFragment)
                        true
                    }

                    R.id.textToTranslateFragment -> {
                        navController.navigate(R.id.textToTranslateFragment)
                        true
                    }

                    R.id.dictionaryFragment -> {
                        navController.navigate(R.id.dictionaryFragment)
                        true
                    }

                    R.id.OCRFragment -> {
                        navController.navigate(R.id.OCRFragment)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupKeyboardVisibilityListener() {
        val rootView = window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            updateBottomNavVisibility()
        }
    }

    private fun updateBottomNavVisibility() {
        if (!shouldShowBottomNav) {
            binding.bottomNavigation.visibility = View.GONE
            binding.customToolbar.toolbar.visibility = View.GONE
            return
        }

        // Check if keyboard is visible
        val rect = Rect()
        window.decorView.rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = window.decorView.rootView.height
        val keypadHeight = screenHeight - rect.bottom
        val isKeyboardVisible = keypadHeight > screenHeight * 0.15

        binding.bottomNavigation.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE
    }

}

