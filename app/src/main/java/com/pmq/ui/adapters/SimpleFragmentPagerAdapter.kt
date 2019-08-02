package com.pmq.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pmq.ui.fragments.JukeboxHomeFragment
import com.pmq.ui.fragments.PlayerFragment
import com.pmq.ui.fragments.SearchFragment

class SimpleFragmentPagerAdapter(val context: Context,
                                 fm : FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        return when(p0){
            0 -> JukeboxHomeFragment.newInstance("test", "test")
            1 -> PlayerFragment.newInstance("test2", "test2")
            else -> SearchFragment.newInstance()
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