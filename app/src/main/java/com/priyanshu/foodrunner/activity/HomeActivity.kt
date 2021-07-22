package com.priyanshu.foodrunner.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.fragment.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var previousMenuItem: MenuItem? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var sharedPreferences: SharedPreferences
    lateinit var txtUserName: TextView
    lateinit var txtPhone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        init()
        setupToolbar()
        setupActionBarToggle()
        displayHome()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun displayHome() {
        val fragment = HomeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }


    private fun setupActionBarToggle() {
        actionBarDrawerToggle =
            object : ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.openDrawer,
                R.string.closeDrawer
            ) {
                override fun onDrawerStateChanged(newState: Int) {
                    super.onDrawerStateChanged(newState)
                    val pendingRunnable = Runnable {
                        val inputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    }
                    Handler().postDelayed(pendingRunnable, 50)
                }
            }

        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun init() {
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val headerView = navigationView.getHeaderView(0);

        txtUserName = headerView.findViewById(R.id.txtDrawerText)
        txtPhone = headerView.findViewById(R.id.txtDrawerSecondaryText)


        val userName = sharedPreferences.getString("name", "Priyanshu").toString()
        val mobileNumber = "+91-${sharedPreferences.getString("mobile_number", "0123456789")}"

        txtUserName.text = userName
        txtPhone.text = mobileNumber
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if (previousMenuItem != null) {
            previousMenuItem?.isChecked = false
        }

        item.isCheckable = true
        item.isChecked = true

        previousMenuItem = item


        val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
        Handler().postDelayed(mPendingRunnable, 100)

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        when (item.itemId) {

            R.id.home -> {
                val homeFragment = HomeFragment()
                fragmentTransaction.replace(R.id.frame, homeFragment)
                fragmentTransaction.commit()
                supportActionBar?.title = "Restaurants"
            }

            R.id.myProfile -> {
                val profileFragment = ProfileFragment()
                fragmentTransaction.replace(R.id.frame, profileFragment)
                fragmentTransaction.commit()
                supportActionBar?.title = "Profile"
            }

            R.id.favRes -> {
                val favFragment = FavouritesFragment()
                fragmentTransaction.replace(R.id.frame, favFragment)
                fragmentTransaction.commit()
                supportActionBar?.title = "Favorite Restaurants"
            }

            R.id.ordHis -> {
                val ordHistoryFragment = OrderHistoryFragment()
                fragmentTransaction.replace(R.id.frame, ordHistoryFragment)
                fragmentTransaction.commit()
                supportActionBar?.title = "My Previous Orders"
            }

            R.id.faqs -> {
                val faqFragment = FAQFragment()
                fragmentTransaction.replace(R.id.frame, faqFragment)
                fragmentTransaction.commit()
                supportActionBar?.title = "Frequently Asked Questions"
            }

            R.id.logout -> {

                val builder = AlertDialog.Builder(this@HomeActivity)
                builder.setTitle("Confirmation")
                    .setMessage("Are you sure you want exit?")
                    .setPositiveButton("Yes") { _, _ ->
                        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                        ActivityCompat.finishAffinity(this)
                    }
                    .setNegativeButton("No") { _, _ ->
                        displayHome()
                    }
                    .create()
                    .show()

            }

        }
        return true
    }
}
