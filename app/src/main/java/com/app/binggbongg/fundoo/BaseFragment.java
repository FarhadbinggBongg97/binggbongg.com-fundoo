package com.app.binggbongg.fundoo;

import androidx.fragment.app.Fragment;

import com.app.binggbongg.helper.callback.OnBackPressedListener;

public class BaseFragment extends Fragment implements OnBackPressedListener {

    @Override
    public boolean onBackPressed() {
        return false;
    }
}