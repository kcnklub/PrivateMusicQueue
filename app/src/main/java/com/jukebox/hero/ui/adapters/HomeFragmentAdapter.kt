package com.jukebox.hero.ui.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.jukebox.hero.ui.fragments.*

class HomeFragmentAdapter(val context: Context, fm : FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(p0: Int): Fragment {
        return when(p0){
            0 -> HomePersonalPartiesFragment.newInstance("test", "test")
            else -> HomeLocalPartiesFragment.newInstance("test2", "test2")
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "personal"
            else -> "local"
        }
    }
}