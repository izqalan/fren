package com.izqalan.messenger;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChecklistFragment extends Fragment {

    private View mainView;
    private RecyclerView checkList;


    public ChecklistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_checklist, container, false);

        checkList = mainView.findViewById(R.id.check_list);
        checkList.setHasFixedSize(true);
        checkList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mainView;

    }

}
