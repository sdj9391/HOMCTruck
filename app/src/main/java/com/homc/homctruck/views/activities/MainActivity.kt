package com.homc.homctruck.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.homc.homctruck.R
import com.homc.homctruck.views.fragments.LoginFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.containerView, LoginFragment(), null)
        fragmentTransaction.commit()*/
    }
}

















