package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class CrimeListFragment extends Fragment {
    private static final int REQUEST_CRIME = 1;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeListRecyclerAdapter mAdapter;

    private int position = -1;
    private boolean mSubtitleVisible;
    private ViewGroup mBlank_placeholder;
    private Button mNew_crime;

    private Callbacks mCallbacks;

    public CrimeListFragment() {
    }

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        View view = inflater.inflate(R.layout.fragment_crime_list_with_blank_placeholder, container, false);
        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mBlank_placeholder = view.findViewById(R.id.blank_placeholder);
        mNew_crime = view.findViewById(R.id.new_crime);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    private void handleBlankPlaceholder() {

        int size = CrimeLab.get(getActivity()).getCrimes().size();
        if (size > 0) {
            mBlank_placeholder.setVisibility(View.INVISIBLE);
        } else {
            mBlank_placeholder.setVisibility(View.VISIBLE);
        }

        mNew_crime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startAddCrime();
                startAddCrimeV2();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem item = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            item.setTitle(R.string.hide_subtitle);
        } else {
            item.setTitle(R.string.show_subtitle);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                startAddCrimeV2();


                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    private void startAddCrimeV2() {
        Crime crime = new Crime();
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        crimeLab.addCrime(crime);
        int position = crimeLab.getCrimes().size() + 1;
        crime.setPosition(position);
        mCallbacks.onCrimeSelected(crime);
        updateUI();
    }

    private void startAddCrime() {
        Crime crime = new Crime();
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        crimeLab.addCrime(crime);

        int position = crimeLab.getCrimes().size() + 1;
        Intent intent = CrimePagerActivity.newStartIntent(getActivity(), crime.getId(), position);
        startActivity(intent);
    }

    private void updateSubtitle() {
        int size = CrimeLab.get(getActivity()).getCrimes().size();
//        String subtitle = getString(R.string.subtitle_format, size);
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, size, size);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        supportActionBar.setSubtitle(subtitle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CRIME && resultCode == Activity.RESULT_OK) {
            position = data.getIntExtra(CrimeActivity.EXTRA_CRIME_POSITION, -1);
        }

    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeListRecyclerAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);


        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();

//            if (position == -1) {
//                mAdapter.notifyDataSetChanged();
//            } else {
//                mAdapter.notifyItemChanged(position);
//            }
        }
        updateSubtitle();
        handleBlankPlaceholder();

    }

    private class CrimeRecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mSolvedImageView;
        private int position = -1;

        public CrimeRecycleViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutResource) {
            super(inflater.inflate(layoutResource, parent, false));

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            startActivity(CrimeActivity.newIntent(getActivity(), mCrime.getId()));
//            startActivityForResult(CrimeActivity.newIntent(getActivity(), mCrime.getId(), position), REQUEST_CRIME);
//            startActivityForResult(CrimePagerActivity.newStartIntent(getActivity(), mCrime.getId(), position), REQUEST_CRIME);

            mCrime.setPosition(position);
            mCallbacks.onCrimeSelected(mCrime);

        }

        public void bind(Crime crime, int position) {
            mCrime = crime;
            this.position = position;
            mTitleTextView.setText(crime.getTitle());
            Date date = crime.getDate();
//            String dateStr = DateFormat.format("EEEE, MMM dd, yyyy", date).toString();
            String dateStr = DateFormat.format(getResources().getString(R.string.date_format), date).toString();
            mDateTextView.setText(dateStr);
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        public Crime getCrime() {
            return mCrime;
        }
    }

    private class CrimeListRecyclerAdapter extends RecyclerView.Adapter<CrimeRecycleViewHolder> {

        private List<Crime> mCrimes;

        public CrimeListRecyclerAdapter(List<Crime> mCrimes) {
            this.mCrimes = mCrimes;


            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    CrimeListRecyclerAdapter.this.onItemDismissed(viewHolder);
                    updateUI();
                }
            });
            itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);

        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeRecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == 0) {
                return new CrimeRecycleViewHolder(layoutInflater, parent, R.layout.list_item_crime);

            } else {
                return new CrimeRecycleViewHolder(layoutInflater, parent, R.layout.list_item_crime_police);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeRecycleViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime, position);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
//            Crime crime = mCrimes.get(position);
//            return crime.isRequiresPolice() ? 1 : 0;

        }

        public void onItemDismissed(RecyclerView.ViewHolder viewHolder) {
            CrimeRecycleViewHolder crimeRecycleViewHolder = (CrimeRecycleViewHolder) viewHolder;
            Crime crime = crimeRecycleViewHolder.getCrime();
            CrimeLab.get(getActivity()).deleteCrime(crime.getId());
            notifyItemRemoved(viewHolder.getAdapterPosition());

        }

    }

}
