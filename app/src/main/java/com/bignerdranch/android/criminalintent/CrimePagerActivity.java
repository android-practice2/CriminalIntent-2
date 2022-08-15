package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";
    public static final String EXTRA_CRIME_POSITION =
            "com.bignerdranch.android.criminalintent.position";


    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);


        mViewPager = findViewById(R.id.crime_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mCrimes = CrimeLab.get(this).getCrimes();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                setupResult(position);
                return new CrimeFragment(crime.getId(), position, mViewPager);
//                return CrimeFragment.newInstance(crime.getId(), position);
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        UUID crimeId =(UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int enterPosition = getIntent().getIntExtra(EXTRA_CRIME_POSITION, -1);
        mViewPager.setCurrentItem(enterPosition);//should place after mViewPager.setAdapter


    }

    private void setupResult(int position) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CRIME_POSITION, position);
        setResult(Activity.RESULT_OK, intent);
    }

    public static Intent newStartIntent(Context context, UUID id, int position) {
        Intent intent = new Intent(context, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, id);
        intent.putExtra(EXTRA_CRIME_POSITION, position);

        return intent;
    }


    @Override
    public void onCrimeUpdated(Crime crime) {

    }
}
