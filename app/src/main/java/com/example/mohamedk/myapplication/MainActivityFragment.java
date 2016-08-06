package com.example.mohamedk.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    private ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        FetchWeatherTask f = new FetchWeatherTask();
        f.execute(getString(R.string.locatoin_pref_default_value));
        setHasOptionsMenu(true);
    }
    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }
    private void updateWeather(){

        FetchWeatherTask f = new FetchWeatherTask();
        // now we want to get the value saved in the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.location_pref_key),
                getString(R.string.locatoin_pref_default_value));
        f.execute(location);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            updateWeather();

        }



            return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_layout_file, // The name of the layout ID.
                        R.id.textlayout, // The ID of the textview to populate.
                        weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }



    // we will make the background thread class


    /*
    the AsyncTask must return a array of strings to pass them to the main thread
     */
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        // we want the log error name be what ever the class name is
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {

            // here we want to check for the check for the unites selected by the user
            SharedPreferences sharedprefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedprefs.getString(
                    getString(R.string.temp_pref_key),getString(R.string.temp_lable_metric)
            );
            if(unitType.equals(getString(R.string.temp_value_imperial))){
                high = (high*1.8)+32;
                low  =(low*1.8)+32;
            }
            else if(!unitType.equals(getString(R.string.temp_value_metric))) // if the value nither nor
               Log.d(LOG_TAG,"Unit type not found"+unitType);


// For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlconnection = null;
            BufferedReader bufferedreader  = null;
            String result = null;


            /*
            declare the uri params here
             */
            String format = "json";
            String units = "metric";
            int numDays = 7;


            try{
                // set the url connections

                // we are not setting the URL params explicitly

                final String URL_Base = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                final String apiKey = "07214a7a1c7de02cd168e2d1411a455c";
                Uri builturl = Uri.parse(URL_Base).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM,apiKey)
                        .build();

             //   http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7

                 // print the url in the log to be sure

                Log.v(LOG_TAG, "Built URI " + builturl.toString());
               // declaring the url
                URL url = new URL(builturl.toString());
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
                    return null; // return null if the buffer was empty;
                }
                result = stringbuffer.toString();


            } catch (IOException e) {
                Log.e("Fragment","Error",e);
                return null;
            }

            // after making the try and catch block you have to make the finally block


            finally{
                // usually the finally block includes the resource closing

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
            try {
                return getWeatherDataFromJson(result,numDays);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null; // this return null happens when networking problem occurs

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mForecastAdapter.clear();
                for(String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

}
