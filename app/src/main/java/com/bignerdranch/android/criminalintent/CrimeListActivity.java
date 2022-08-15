package com.bignerdranch.android.criminalintent;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class CrimeListActivity extends SingleFragmentActivity  implements CrimeListFragment.Callbacks
, CrimeFragment.Callbacks{
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {

        if (findViewById(R.id.detail_fragment_container) == null) {
            int position = crime.getPosition();
            Intent intent = CrimePagerActivity.newStartIntent(this, crime.getId(), position);
            startActivity(intent);
        }else{
            FragmentManager fragmentManager =getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.detail_fragment_container, CrimeFragment.newInstance(crime.getId(), crime.getPosition()))
                    .commit();
        }


    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment fragment =(CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.updateUI();

    }
}
