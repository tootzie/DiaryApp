package com.example.diaryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_nav_drawer.*

class ActivityNavDrawer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var mToggle : ActionBarDrawerToggle

    lateinit var mfSettings : SettingFragment
    lateinit var mfList : ListDiary




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_drawer)

        // memunculkan tombol burger menu
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // untuk toggle open dan close navigation
        mToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)

        // tambahkan mToggle ke drawer_layout sebagai pengendali open dan close drawer
        drawer_layout.addDrawerListener(mToggle)
        mToggle.syncState()

        //Handle click pada menu- menu nav drawer
        nav_view.setNavigationItemSelectedListener(this)

        //Default Fragment yg dibuka pertama kali
        viewfragment(3)
    }

    fun viewfragment(nomer: Number) {
        if(nomer == 1) { //Settings
            val mFragmentManagement = supportFragmentManager
            mfSettings = SettingFragment()

            val fragment =
                mFragmentManagement?.beginTransaction()?.apply {
                    replace(R.id.contentFrag,mfSettings,SettingFragment::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
        }
        else if(nomer == 2) { //New Diary
            val pIntent1 = Intent(this@ActivityNavDrawer, newStory::class.java)
            startActivity(pIntent1)
        }
        else if(nomer == 3) { //List Diary
            val mFragmentManagement = supportFragmentManager
            mfList = ListDiary()

            val fragment =
                mFragmentManagement?.beginTransaction()?.apply {
                    replace(R.id.contentFrag,mfList, ListDiary::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        //Toast.makeText(baseContext, "di option item", Toast.LENGTH_SHORT).show()
        return mToggle.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menunav_Settings -> {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                viewfragment(1)
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                true
            }
            R.id.menunav_newDiary -> {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                viewfragment(2)
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                true
            }
            R.id.menunav_List -> {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                viewfragment(3)
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                true
            }
            else -> true
        }
        return true
    }
}