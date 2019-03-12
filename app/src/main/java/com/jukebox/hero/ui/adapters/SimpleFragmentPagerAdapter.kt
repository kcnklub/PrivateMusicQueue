package com.jukebox.hero.ui.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.jukebox.hero.ui.fragments.PlayerFragment
import com.jukebox.hero.ui.PartyViewActivity

class SimpleFragmentPagerAdapter(val context: Context,
                                 fm : FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        return when(p0){
            0 -> PlayerFragment.newInstance("test", "test")
            1 -> PlayerFragment.newInstance("test2", "test2")
            else -> PlayerFragment.newInstance("test3", "test3")
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "test"
            1 -> "test2"
            else -> "test3"
        }
    }
}