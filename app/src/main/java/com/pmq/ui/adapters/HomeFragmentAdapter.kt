package com.pmq.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pmq.ui.fragments.HomeLocalPartiesFragment
import com.pmq.ui.fragments.HomePersonalPartiesFragment

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