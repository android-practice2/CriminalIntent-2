package com.bignerdranch.android.criminalintent;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;
@Deprecated //use  CrimePagerActivity
public class CrimeActivity extends SingleFragmentActivity {
    public static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";
    public static final String EXTRA_CRIME_POSITION =
            "com.bignerdranch.android.criminalintent.position";

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        UUID id = (UUID) intent.getSerializableExtra(EXTRA_CRIME_ID);
        int position = getIntent().getIntExtra(EXTRA_CRIME_POSITION, -1);

        setupResult();
        return CrimeFragment.newInstance(id, position);
    }

    private void setupResult() {
        Intent intent = new Intent();
        int position = getIntent().getIntExtra(EXTRA_CRIME_POSITION, -1);
        intent.putExtra(EXTRA_CRIME_POSITION, position);

        setResult(Activity.RESULT_OK, intent);
    }


    public static Intent newIntent(Context context, UUID id, Integer position) {
        Intent intent = new Intent(context, CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, id);
        intent.putExtra(EXTRA_CRIME_POSITION, position);
        return intent;

    }



}