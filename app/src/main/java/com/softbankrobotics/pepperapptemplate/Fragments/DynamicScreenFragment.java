package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;
import com.softbankrobotics.pepperapptemplate.TileItem;

import java.io.IOException;
import java.io.InputStream;

public class DynamicScreenFragment extends Fragment {

    private static final String TAG = "MSI_DynamicScreen";
    private static final String ARG_TILE_INDEX = "tile_index";
    private MainActivity ma;

    public static DynamicScreenFragment newInstance(int tileIndex) {
        DynamicScreenFragment fragment = new DynamicScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TILE_INDEX, tileIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public int getTileIndex() {
        return getArguments() != null ? getArguments().getInt(ARG_TILE_INDEX, 0) : 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(R.layout.fragment_dynamic_screen, container, false);
            } else {
                return inflater.inflate(R.layout.fragment_dynamic_screen, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int index = getTileIndex();
        if (ma.getTileItems() == null || index < 0 || index >= ma.getTileItems().size()) {
            Log.e(TAG, "Invalid tile index or missing content");
            ma.setFragment(new MainFragment());
            return;
        }
        TileItem tile = ma.getTileItems().get(index);

        // Set title with number prefix
        TextView title = view.findViewById(R.id.dynamic_title);
        title.setText((index + 1) + ". " + tile.getTitle());

        // Set optional image
        ImageView imageView = view.findViewById(R.id.dynamic_image);
        if (tile.getImageName() != null) {
            try {
                InputStream is = ma.getAssets().open("images/" + tile.getImageName());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Log.w(TAG, "Image not found: " + tile.getImageName());
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }

        // "Opakovat rec" button
        view.findViewById(R.id.dynamic_say_button).setOnClickListener(v ->
                ma.getCurrentChatBot().goToBookmarkSameTopic("say"));

        // "Uvodni obrazovka" button
        view.findViewById(R.id.dynamic_reset_button).setOnClickListener(v ->
                ma.setFragment(new MainFragment()));
    }
}
