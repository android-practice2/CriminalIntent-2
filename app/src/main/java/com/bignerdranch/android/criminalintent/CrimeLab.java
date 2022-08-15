package com.bignerdranch.android.criminalintent;

import static com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext=context.getApplicationContext();
        mDatabase=new CrimeBaseHelper(context).getWritableDatabase();

    }

    public Crime getCrime(UUID uuid) {
        CrimeCursorWrapper cursor = query(CrimeTable.Cols.UUID + "=?", new String[]{uuid.toString()});
        Crime crime;
        try {
            cursor.moveToFirst();
            crime = cursor.getCrime();
        } finally {
            cursor.close();
        }

        return crime;

    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = query(null, null);
        try {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);
        mDatabase.insert(CrimeTable.NAME, null, contentValues);

    }

    public void updateCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, contentValues, CrimeTable.Cols.UUID+"=?", new String[]{crime.getId().toString()});
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

    private CrimeCursorWrapper query(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null

        );
        return  new CrimeCursorWrapper(cursor);
    }


    public void deleteCrime(UUID id) {
        mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + "=?", new String[]{id.toString()});
    }



    private static ContentValues getContentValues(Crime crime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeTable.Cols.UUID,crime.getId().toString());
        contentValues.put(CrimeTable.Cols.TITLE,crime.getTitle());
        contentValues.put(CrimeTable.Cols.DATE,crime.getDate().getTime());
        contentValues.put(CrimeTable.Cols.SOLVED,crime.isSolved()?1:0);
        contentValues.put(CrimeTable.Cols.SUSPECT,crime.getSuspect());
        contentValues.put(CrimeTable.Cols.SUSPECT_PHONE_NUMBER,crime.getSuspectPhoneNumber());

        return contentValues;

    }
}