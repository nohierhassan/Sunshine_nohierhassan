package com.example.mohamedk.myapplication;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // first create the array list:

        // first cerate the array
        String [] data ={
          "Mohamed","Ahmed","Mahmoud","Nohier","Hassan","Mostafa","Anas","Hammada",
                "Mohamed","Ahmed","Mahmoud","Nohier","Hassan","Mostafa","Anas","Hammada"
        };
         // then create the list
        List <String> weekForecast = new ArrayList<String>(Arrays.asList(data));
       // we will use the inflator in order to be able to rever to the listview in the layout file of the faragment



        // create the addapter
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.list_layout_file, R.id.textlayout,weekForecast // these id's located in the design stage
        );
        // now use the inflator
        View rootView = inflater.inflate(R.layout.fragment_main, container,false);

        ListView listview = (ListView)rootView.findViewById(R.id.listview_forecast); // the id of the listview in the fragment
        listview.setAdapter(mForecastAdapter);
        return rootView;
    }
}
