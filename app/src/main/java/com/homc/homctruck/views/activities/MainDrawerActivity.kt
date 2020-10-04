package com.homc.homctruck.views.activities

import android.content.Intent
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
import com.homc.homctruck.utils.showConfirmDialog
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
            R.id.navFindTruck, R.id.navAddLoad, R.id.navFindLoad
        )*/
        navController = findNavController(R.id.navHostFragment)
        appBarConfig = AppBarConfiguration(navController.graph, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfig)
        navigationView.setupWithNavController(navController)
        navigationView.menu.findItem(R.id.logoutAction).setOnMenuItemClickListener {
            onLogoutClicked()
            return@setOnMenuItemClickListener true
        }
    }

    private fun onLogoutClicked() {
        showConfirmDialog(
            this, getString(R.string.msg_confirm_logout),
            { _, _ -> removeAccount() }, null,
            getString(R.string.label_done), getString(R.string.label_cancel)
        )
    }

    private fun removeAccount() {
        BaseAccountManager(this).userDetails = null
        BaseAccountManager(this).userAuthToken = null
        BaseAccountManager(this).isMobileVerified = null
        BaseAccountManager(this).removeAccount(this)
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
