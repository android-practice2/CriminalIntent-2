package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    public static final String ARG_CRIME_ID = "crime_id";
    public static final String ARG_POSITION = "position";
    private static final String DIALOG_DATE = "DialogDate";
    public static final int REQUEST_CODE_DATE = 0;
    public static final int REQUEST_CODE_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    public static final int REQUEST_CODE_REQ_PERM_READ_CONTACTS = 4;
    public static final String TAG_DIALOG_TIME = "tag_dialog_time";
    public static final String ARG_CONTACTS_ID = "arg_contactsId";
    public static final String AUTHORITY = "com.bignerdranch.android.criminalintent.fileprovider";
    public static final String TAG_IMAGE_ZOOM_IN = "tag_image_zoom_in";

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;

    private Button jump_to_first;
    private Button jump_to_last;
    private ViewPager mViewPager;
    private int mPosition;

    private Button mCrimeTimeButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mDialSuspectButton;

    private ImageButton mPhotoView;
    private ImageButton mPhotoButton;

    private File mPhotoFile;
    private Callbacks mCallbacks;

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks=(Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks=null;
    }

    public static CrimeFragment newInstance(UUID id, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CRIME_ID, id);
        bundle.putSerializable(ARG_POSITION, position);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public CrimeFragment() {
    }


    public CrimeFragment(UUID id, int position, ViewPager viewPager) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CRIME_ID, id);
        mPosition = position;
        bundle.putSerializable(ARG_POSITION, mPosition);
        this.setArguments(bundle);

        this.mViewPager = viewPager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        UUID id = (UUID) arguments.getSerializable(ARG_CRIME_ID);
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        mCrime = crimeLab.getCrime(id);

        setHasOptionsMenu(true);

        mPhotoFile = crimeLab.getPhotoFile(mCrime);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        handleTitleField(view);

        handleDateButton(view);

        handleSolvedCheckBox(view);


        handleJump(view);

        handleTimeButton(view);
        updateDate();


        handleReportButton(view);


        handleSuspectButton(view);
        handleDialSuspectButton(view);
        updateSuspect();

        handlePhotoButton(view);

        handlePhotoView(view);

        return view;

    }

    private void handlePhotoView(View view) {
        mPhotoView = view.findViewById(R.id.crime_photo);
        ViewTreeObserver viewTreeObserver = mPhotoView.getViewTreeObserver();
//        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                updatePhotoView();
//            }
//        });

        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPhotoView.getViewTreeObserver() .removeOnPreDrawListener(this);
                updatePhotoView();
                return true;
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    ImageZoomInFragment fragment = ImageZoomInFragment.newInstance(mPhotoFile.getPath());
                    fragment.show(getFragmentManager(), TAG_IMAGE_ZOOM_IN);
                }
            }
        });
    }

    private void handlePhotoButton(View view) {
        mPhotoButton = view.findViewById(R.id.crime_camera);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoButton.setEnabled(
                mPhotoFile != null
//                        &&intent.resolveActivity(getActivity().getPackageManager())!=null//always null,may by caused by API compact problem
        );
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = getFileUri();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                //grant permission
                for (ResolveInfo resolveInfo : resolveInfos) {

                    getActivity().grantUriPermission(resolveInfo.activityInfo.packageName
                            , uri
                            , Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );
                }
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });
    }

    private Uri getFileUri() {
        return FileProvider.getUriForFile(getActivity(),
                AUTHORITY, mPhotoFile);
    }

    private void updatePhotoView() {
        if (mPhotoFile != null && mPhotoFile.exists()) {

//            Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight());
            mPhotoView.setImageBitmap(scaledBitmap);
        } else {
            mPhotoView.setImageDrawable(null);

        }
    }

    private void handleTitleField(View view) {
        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void handleSolvedCheckBox(View view) {
        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();

            }
        });
    }

    private void handleDateButton(View view) {
        mDateButton = view.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = isTablet();
                if (b) {//tablet
                    startDatePickerDialog();

                } else {//phone
                    startActivityForResult(DatePickerActivity.newIntent(getActivity(), mCrime.getDate()), REQUEST_CODE_DATE);
                }

            }
        });
    }

    private void handleDialSuspectButton(View view) {
        mDialSuspectButton = view.findViewById(R.id.dial_suspect);

        if (mCrime.getSuspect() == null) {
            mDialSuspectButton.setEnabled(false);
        } else {
            mDialSuspectButton.setEnabled(true);
        }

        mDialSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mCrime.getSuspectPhoneNumber();

                Uri uri = Uri.parse("tel:" + phoneNumber);
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
            }
        });
    }

    private void handleSuspectButton(View view) {
        mSuspectButton = view.findViewById(R.id.crime_suspect);
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        checkContactAppExistence(intent);


        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CONTACT);

            }
        });
    }

    private void checkContactAppExistence(Intent intent) {
        PackageManager packageManager = getActivity().getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo == null) {//always null. maybe cause of API compact problem
            mSuspectButton.setEnabled(false);
        } else {
            mSuspectButton.setEnabled(true);

        }
    }

    private void handleReportButton(View view) {
        mReportButton = view.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new ShareCompat.IntentBuilder(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
//                        .getIntent();
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();
//                Intent intent = buildNativeActionSendIntent();

                startActivity(intent);
            }
        });
    }

    @NonNull
    private Intent buildNativeActionSendIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
        intent.setType("text/plain");
        intent = Intent.createChooser(intent, getString(R.string.send_report));
        return intent;
    }

    private void updateSuspect() {
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
            mDialSuspectButton.setEnabled(true);
        } else {
            mDialSuspectButton.setEnabled(false);

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_crime, menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime.getId());
                getActivity().finish();

                return true;
            default:

                return super.onOptionsItemSelected(item);
        }

    }

    private boolean isTablet() {
        boolean aBoolean = getResources().getBoolean(R.bool.isTablet);
        return aBoolean;
    }

    private void startDatePickerDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(mCrime.getDate());
        datePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_CODE_DATE);
        datePickerFragment.show(fragmentManager, DIALOG_DATE);
    }

    private void handleTimeButton(View view) {
        mCrimeTimeButton = view.findViewById(R.id.crime_time);
        mCrimeTimeButton.setText(DateFormat.format("HH:mm", mCrime.getDate()).toString());
        mCrimeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerFragment fragment = new TimePickerFragment(mCrime.getDate());

                fragment.setTargetFragment(CrimeFragment.this, REQUEST_CODE_TIME);
                fragment.show(getFragmentManager(), TAG_DIALOG_TIME);

            }
        });
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);

    }

    private void updateDate() {
        String dateStr = DateFormat.format(getResources().getString(R.string.date_format), mCrime.getDate()).toString();
        mDateButton.setText(dateStr);
        mCrimeTimeButton.setText(DateFormat.format("HH:mm", mCrime.getDate()).toString());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        } else if (requestCode == REQUEST_CODE_TIME) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        } else if (requestCode == REQUEST_CONTACT) {
            settleSuspectResult(data);

            updateCrime();

        } else if (requestCode == REQUEST_PHOTO) {

            //revoke permission
            Uri uri = getFileUri();
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
            updateCrime();


        }


    }

    private void settleSuspectResult(@Nullable Intent data) {
        Uri uri = data.getData();

        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(uri,
                new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME
                        , ContactsContract.Contacts._ID

                }, null, null, null);

        try {
            if (cursor.getCount() == 0) {
                return;
            }
            cursor.moveToFirst();
            String suspect = cursor.getString(0);
            mCrime.setSuspect(suspect);
            updateSuspect();

            long contactsId = cursor.getLong(1);
//            bundleContactsId(contactsId);
//            long contactsId = getArguments().getLong(ARG_CONTACTS_ID);
            settlePhoneNum(contactsId);

        } finally {
            cursor.close();
        }

//        requestPermission();

    }

    private void requestPermission() {
        //check permission
        int permissionGrant = ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_CONTACTS);
        if (permissionGrant != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_REQ_PERM_READ_CONTACTS);

        } else {
            Toast.makeText(getActivity(), "Permission already granted", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_REQ_PERM_READ_CONTACTS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "permission granted", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_SHORT).show();
            }


        }


    }

    private void settlePhoneNum(long contactsId) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(uri,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone._ID
                        , ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                ContactsContract.CommonDataKinds.Phone._ID + "=?",
                new String[]{String.valueOf(contactsId)},
                null
        );
        try {
            if (cursor.getCount() == 0) {
                return;
            }
            cursor.moveToFirst();
            String phoneNumber = cursor.getString(1);
            mCrime.setSuspectPhoneNumber(phoneNumber);

        } finally {
            cursor.close();
        }
    }

    private void bundleContactsId(long contactsId) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        arguments.putLong(ARG_CONTACTS_ID, contactsId);
    }

    private void handleJump(View view) {
        jump_to_first = view.findViewById(R.id.jump_to_first);
        jump_to_last = view.findViewById(R.id.jump_to_last);

        jump_to_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });

        int size = CrimeLab.get(getActivity()).getCrimes().size();

        jump_to_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(size - 1);

            }
        });

        if (mPosition == 0) {
            jump_to_first.setEnabled(false);
        } else {
            jump_to_first.setEnabled(true);
        }

        if (mPosition >= size - 1) {
            jump_to_last.setEnabled(false);
        } else {
            jump_to_last.setEnabled(true);
        }

    }

    public String getCrimeReport() {
        return getString(R.string.crime_report,
                mCrime.getTitle()
                , DateFormat.format(getResources().getString(R.string.date_format), mCrime.getDate()).toString()
                , mCrime.isSolved() ? getString(R.string.crime_report_solved) : getString(R.string.crime_report_unsolved)
                , mCrime.getSuspect() != null ? getString(R.string.crime_report_suspect, mCrime.getSuspect()) : getString(R.string.crime_report_no_suspect)
        );
    }
}
