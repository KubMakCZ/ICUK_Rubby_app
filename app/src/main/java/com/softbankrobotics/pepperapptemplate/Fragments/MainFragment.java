package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;

public class MainFragment extends Fragment {

    private static final String TAG = "MSI_MainFragment";
    private MainActivity ma;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.main_button_frag_three).setOnClickListener(
                (v) -> ma.setFragment(new ScreenThreeFragment()));
        view.findViewById(R.id.main_button_frag_four).setOnClickListener(
                (v) -> ma.setFragment(new ScreenFourFragment()));
        view.findViewById(R.id.main_button_frag_five).setOnClickListener(
                (v) -> ma.setFragment(new ScreenFiveFragment()));
        view.findViewById(R.id.main_button_frag_six).setOnClickListener(
                (v) -> ma.setFragment(new ScreenSixFragment()));
        view.findViewById(R.id.main_button_frag_seven).setOnClickListener(
                (v) -> ma.setFragment(new ScreenSevenFragment()));
        view.findViewById(R.id.main_button_frag_eight).setOnClickListener(
                (v) -> ma.setFragment(new ScreenEightFragment()));
        view.findViewById(R.id.main_button_frag_nine).setOnClickListener(
                (v) -> ma.setFragment(new ScreenNineFragment()));
        view.findViewById(R.id.main_button_frag_ten).setOnClickListener(
                (v) -> ma.setFragment(new ScreenTenFragment()));
    }

}
