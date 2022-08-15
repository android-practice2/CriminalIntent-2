package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.zip.Inflater;

public class ImageZoomInFragment extends DialogFragment {

    private ImageView zoom_in_image_view;

    public static final String ARGS_PATH = "args_path";

    public static ImageZoomInFragment newInstance(String path) {
        ImageZoomInFragment fragment = new ImageZoomInFragment();

        Bundle args = new Bundle();
        args.putString(ARGS_PATH,path);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        String path = getArguments().getString(ARGS_PATH);

        View layout = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_image_zoom_in, null, false);
        zoom_in_image_view = layout.findViewById(R.id.zoom_in_image_view);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        zoom_in_image_view.setImageBitmap(bitmap);

        return  new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

    }
}
