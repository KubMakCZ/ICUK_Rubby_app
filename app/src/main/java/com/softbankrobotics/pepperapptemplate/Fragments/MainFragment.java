package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;
import com.softbankrobotics.pepperapptemplate.TileAdapter;
import com.softbankrobotics.pepperapptemplate.TileItem;

import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MSI_MainFragment";
    private MainActivity ma;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_main;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            } else {
                return inflater.inflate(fragmentId, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<TileItem> tileItems = ma.getTileItems();

        RecyclerView recyclerView = view.findViewById(R.id.tile_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        if (tileItems != null && !tileItems.isEmpty()) {
            TileAdapter adapter = new TileAdapter(tileItems, index -> {
                ma.setFragment(DynamicScreenFragment.newInstance(index));
            });
            recyclerView.setAdapter(adapter);
        } else {
            TextView mainText = view.findViewById(R.id.main_text);
            mainText.setText("Chybi soubor content.md nebo je prazdny.");
        }
    }
}
