package com.example.paulmanning.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 /**
 * A Forecast fragment containing a simple view.
 * Created by paulmanning on 1/22/15.
 */
public class ForecastFragment extends Fragment {
    public static ArrayAdapter<String> forecastAdapter;
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    final String locationBaseURI = "geo:0,0";
    final String QUERY_PARAM = "q";

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        String location = getLocationString();
        updateWeather(location);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        String location = getLocationString();
        if (id == R.id.action_refresh) {
            updateWeather(location);
            return true;
        }

        if (id == R.id.action_show_map) {

            openPreferredLocationInMap(location);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(String location) {
        Uri geoLocation = Uri.parse(locationBaseURI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.e(LOG_TAG,"Could not map " + location);
        }
    }

    private String getLocationString() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        return sharedPref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
    }

    private void updateWeather(String location) {
        new FetchWeatherTask().execute(location);
//            new FetchWeatherTask().execute("94043");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



//            String weatherJson = getWeatherData("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
//        new FetchWeatherTask().execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
//        new FetchWeatherTask().execute("94043");

//        ArrayList<String> names= new ArrayList<String>();
//        names.add("Today - Sunny - 88/63");
//        names.add("Tomorrow - Foggy - 70/46");
//        names.add("Weds - Cloudy - 72/63");
//        names.add("Thurs - Rainy - 64/51");
//        names.add("Fri - Foggy - 70/46");
//        names.add("Sat - Sunny - 76/68");
//        names.add("Next Sun - Clear - 90/30");
//        names.add("Next Today - Sunny - 88/63");
//        names.add("Next Tomorrow - Foggy - 70/46");
//        names.add("Next Weds - Cloudy - 72/63");
//        names.add("Next Thurs - Rainy - 64/51");
//        names.add("Next Fri - Foggy - 70/46");
//        names.add("Next Sat - Sunny - 76/68");
//        names.add("Next Sun - Clear - 90/30");
//
//        forecastAdapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.list_item_forecast,
//                R.id.list_item_forecast_textview,
//                names);

        forecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView theListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        theListView.setAdapter(forecastAdapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = forecastAdapter.getItem(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity2.class).putExtra(Intent.EXTRA_TEXT,forecast);
//                detailIntent.putExtra("forecastDetail", forecast);
                if (detailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(detailIntent);
                }
                else {
                    Log.e(LOG_TAG,"Could not show detail for " + forecast);
                }
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
        final String QUERY_PARAM = "q";
        final String MODE_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        String outputFormat = "jason";
        String units = "metric";
        int numDays = 10;

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length > 0) {
                try {
                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(MODE_PARAM, outputFormat)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .build();
                    URL url = new URL(builtUri.toString());
//                    Log.v(LOG_TAG, "URL='" + builtUri.toString() + "'");
                    String resultingWeatherJason = getWeatherData(builtUri.toString());
//                myJasonParse(resultingWeatherJason);
                    try {
                        return getWeatherDataFromJson(resultingWeatherJason, numDays);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
                catch (MalformedURLException e)
                {
                    Log.e(LOG_TAG, e.getMessage(),e);
                    e.printStackTrace();
                   return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] resultingWeatherJason) {
            if (resultingWeatherJason != null) {
                forecastAdapter.clear();
//                forecastAdapter.addAll(resultingWeatherJason);  // for honeyComb (3.0) or above
                for (String dayForecastStr : resultingWeatherJason) {
                    forecastAdapter.add(dayForecastStr);
                }
            }
        }

        protected void myJasonParse(String resultingWeatherJason) {
//            mImageView.setImageBitmap(result);
//            Log.v(LOG_TAG, "on post Result of weather request: " + resultingWeatherJason);
            String minItem = "";
            String maxItem = "";
            String mainItem = "";

            try {
                JSONObject jObject = new JSONObject(resultingWeatherJason);
//                Log.v(LOG_TAG, "jobject built");
                JSONArray jArray = jObject.getJSONArray("list");
//                Log.v(LOG_TAG, "got list");
                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);
//                        Log.v(LOG_TAG, "got array from list");
                        // Pulling items from the array
                        String dtItem = oneObject.getString("dt");
//                        Log.v(LOG_TAG, "got dt:"+dtItem);

                        JSONObject tempItem = oneObject.getJSONObject("temp");
//                        Log.v(LOG_TAG, "got temp");

                        minItem = tempItem.getString("min");
//                        Log.v(LOG_TAG, "got min:"+minItem);

                        maxItem = tempItem.getString("max");
//                        Log.v(LOG_TAG, "got max:"+maxItem);

                        JSONArray weatherObject = oneObject.getJSONArray("weather");
//                        Log.v(LOG_TAG, "got weather");
                        JSONObject weatherItem = weatherObject.getJSONObject(0);
                        mainItem = weatherItem.getString("main");
//                        Log.v(LOG_TAG, "got main:"+mainItem);

                        Log.v(LOG_TAG, String.format("dt %s min %s max %s main %s",dtItem, minItem, maxItem, mainItem));

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(),e);
                        e.printStackTrace();
                    }
                }
            }
            catch(JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(),e);
                e.printStackTrace();
            }

        }

        private String getWeatherData(String weatherRequestUrl) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(weatherRequestUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
//            Log.v(LOG_TAG, "Result of weather request: " + forecastJsonStr);
            return forecastJsonStr;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        private double convertCelsiusToFahrenheit(double celsius) {
            return ((celsius * 9) / 5) + 32;
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String units = sharedPref.getString(getString(R.string.pref_units_key),
                    "2");

            if (units.equals("2")) {
                high = convertCelsiusToFahrenheit(high);
                low = convertCelsiusToFahrenheit(low);
            }
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
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

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
                long dateTime = dayForecast.getLong(OWM_DATETIME);
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

            return resultStrs;
        }
    }
}