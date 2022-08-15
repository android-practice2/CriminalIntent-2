package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.util.Date;

public class DatePickerActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        Date date = (Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
        return DatePickerFragment.newInstance(date);
    }

    public static Intent newIntent(Context context, Date date) {
        Intent intent = new Intent(context, DatePickerActivity.class);
        intent.putExtra(DatePickerFragment.EXTRA_DATE, date);

        return intent;
    }
}
