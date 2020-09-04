package com.homc.homctruck.views.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.homc.homctruck.R
import com.homc.homctruck.data.models.getName
import com.homc.homctruck.utils.account.BaseAccountManager
import kotlinx.android.synthetic.main.activity_main_drawer.*


class MainDrawerActivity : BaseAppActivity() {
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_drawer)
        initToolbar()
        setToolBarTitle(getString(R.string.menu_home))
        setHearViewData()
        // TODO: Use to replace fragment into container when drawer option click
        /*val topLevelDestinations = setOf(
            R.id.navHome, R.id.navAddTruckRout,
            R.id.navSearchTruck, R.id.navAddLoad, R.id.navSearchLoad
        )*/
        navController = findNavController(R.id.navHostFragment)
        appBarConfig = AppBarConfiguration(navController.graph, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfig)
        navigationView.setupWithNavController(navController)
    }

    private fun setHearViewData() {
        val headerView: View = navigationView.getHeaderView(0)
        val titleTextView: TextView? = headerView.findViewById(R.id.titleTextView)
        val subtitleTextView: TextView? = headerView.findViewById(R.id.subtitleTextView)
        val user = BaseAccountManager(this).userDetails
        val name = user?.getName()
        if (name.isNullOrBlank()) {
            titleTextView?.text = getString(R.string.placeholder_plus_91, user?.mobileNumber)
            subtitleTextView?.visibility = View.GONE
        } else {
            subtitleTextView?.visibility = View.VISIBLE
            titleTextView?.text = name
            subtitleTextView?.text = getString(R.string.placeholder_plus_91, user.mobileNumber)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
