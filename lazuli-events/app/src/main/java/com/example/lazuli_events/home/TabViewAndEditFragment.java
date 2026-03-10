package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;


public class TabViewAndEditFragment extends Fragment {


    public TabViewAndEditFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_view_and_edit, container, false);
    }
}