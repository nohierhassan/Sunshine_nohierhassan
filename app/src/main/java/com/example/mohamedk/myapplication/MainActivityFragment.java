package com.example.mohamedk.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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


       // we will use the inflator in order to be able to rever to the listview in the layout file of the faragment

        // here all the parsing code


        HttpURLConnection urlconnection = null;
        BufferedReader bufferedreader  = null;
        String result = null;

        try{
            // set the url connections
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
            urlconnection = (HttpURLConnection)url.openConnection();
            urlconnection.setRequestMethod("GET");
            urlconnection.connect();

            // getting the data

            InputStream inputstream = urlconnection.getInputStream();
            StringBuffer stringbuffer = new StringBuffer();
            if(inputstream == null)
                // here we return null in order to goto the catch block
            {
                return null;

            }

            // now we stroing the data
            bufferedreader = new BufferedReader(new InputStreamReader(inputstream));

            String line;
            while((line = bufferedreader.readLine())!= null){
                stringbuffer.append(line+"\n");
            }
            // check if the stringbuffer length is 0
            // which means error alos
            if(stringbuffer.length()==0)
            {
                return null;
            }
            result = stringbuffer.toString();


        } catch (IOException e) {
            Log.e("Fragment","Error",e);
            return null;
        }

        // after making the try and catch block you have to make the finally block


        finally{
            // usually the finally block includes the resurce closing

            // also we will check if there null there is no need to close them


            /*
            close the HttpURLConnection resource
            * */
            if(urlconnection != null){
                urlconnection.disconnect();

            }
             /*
             close the BufferedReader object

              */
            if(bufferedreader != null){

                try {
                    bufferedreader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


         }













        // create the addapter
        //mForecastAdapter = new ArrayAdapter<String>(
          //      getActivity(),R.layout.list_layout_file, R.id.textlayout,weekForecast // these id's located in the design stage
       // );
        // now use the inflator
        View rootView = inflater.inflate(R.layout.fragment_main, container,false);

        ListView listview = (ListView)rootView.findViewById(R.id.listview_forecast); // the id of the listview in the fragment
        listview.setAdapter(mForecastAdapter);
        return rootView;
    }
}
