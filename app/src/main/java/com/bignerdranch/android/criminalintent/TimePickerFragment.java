package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment {

    private static final String ARG_TIME = "arg_time";
    private TimePicker mTimePicker;

    public TimePickerFragment() {
    }

    public TimePickerFragment(Date date) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);
        this.setArguments(args);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        mTimePicker = layout.findViewById(R.id.dialog_time_picker);
        mTimePicker.setIs24HourView(true);

        Date time = (Date) getArguments().getSerializable(ARG_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        return new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Date date = (Date) getArguments().getSerializable(ARG_TIME);

                        int hour = mTimePicker.getCurrentHour();
                        int minute = mTimePicker.getCurrentMinute();
                        Calendar instance = Calendar.getInstance();
                        instance.setTime(date);
                        instance.set(Calendar.HOUR_OF_DAY, hour);
                        instance.set(Calendar.MINUTE, minute);

                        Date newDate = instance.getTime();

                        sendResult(newDate);

                    }
                })

                .create();
    }

    private void sendResult(Date newDate) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment == null) {
            return;
        }
        Intent data = new Intent();
        data.putExtra(DatePickerFragment.EXTRA_DATE, newDate);
        targetFragment.onActivityResult(CrimeFragment.REQUEST_CODE_TIME, Activity.RESULT_OK, data);
    }
}
