package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    private static final String ARG_DATE = "date";
    public static final String EXTRA_DATE =
            "com.bignerdranch.android.criminalintent.date";
    private DatePicker mDatePicker;
    private Button mDialogOkButton;

    public static DatePickerFragment newInstance(Date date) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        datePickerFragment.setArguments(args);
        return datePickerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (isTablet()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);


//        View view = inflater.inflate(R.layout.dialog_date, container,false);
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);
        mDatePicker = view.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );

        mDialogOkButton = view.findViewById(R.id.dialog_ok_button);
        mDialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GregorianCalendar gregorianCalendar = new GregorianCalendar(
                        mDatePicker.getYear(),
                        mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth()
                );
                Date time = gregorianCalendar.getTime();

                sendResult(time, Activity.RESULT_OK);

                getActivity().finish();


            }
        });

        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);
        mDatePicker = view.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        GregorianCalendar gregorianCalendar = new GregorianCalendar(
                                mDatePicker.getYear(),
                                mDatePicker.getMonth(),
                                mDatePicker.getDayOfMonth()
                        );
                        Date time = gregorianCalendar.getTime();

                        sendResult(time, Activity.RESULT_OK);

                    }
                })
                .create();
    }

    private boolean isTablet() {
//        TelephonyManager systemService = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//        int phoneType = systemService.getPhoneType();
//        boolean b = phoneType == TelephonyManager.PHONE_TYPE_NONE;

        boolean aBoolean = getResources().getBoolean(R.bool.isTablet);
        return aBoolean;
    }

    private void sendResult(Date time, int resultCode) {
        Fragment targetFragment = getTargetFragment();
        Intent data = new Intent();
        data.putExtra(EXTRA_DATE, time);

        if (targetFragment == null) {
            getActivity().setResult(resultCode, data);
            return;
        }

        targetFragment.onActivityResult(CrimeFragment.REQUEST_CODE_DATE, resultCode, data);
    }
}
