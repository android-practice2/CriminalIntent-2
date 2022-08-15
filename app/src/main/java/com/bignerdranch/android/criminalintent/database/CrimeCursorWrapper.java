package com.bignerdranch.android.criminalintent.database;

import static com.bignerdranch.android.criminalintent.database.CrimeDbSchema.*;

import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;

import com.bignerdranch.android.criminalintent.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {


    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        UUID uuid = UUID.fromString(uuidString);
        Crime crime = new Crime(uuid);

        crime.setTitle(getString(getColumnIndex(CrimeTable.Cols.TITLE)));

        long aLong = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        Date date = new Date(aLong);
        crime.setDate(date);

        int anInt = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        crime.setSolved(anInt == 1);

        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        crime.setSuspect(suspect);

        String suspectPhoneNumber = getString(getColumnIndex(CrimeTable.Cols.SUSPECT_PHONE_NUMBER));
        crime.setSuspectPhoneNumber(suspectPhoneNumber);

        return crime;

    }
}

