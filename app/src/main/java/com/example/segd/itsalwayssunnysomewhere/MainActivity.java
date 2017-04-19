
package com.example.segd.itsalwayssunnysomewhere;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.example.segd.itsalwayssunnysomewhere.R;
import com.example.segd.itsalwayssunnysomewhere.data.SunshinePreferences;
import com.example.segd.itsalwayssunnysomewhere.utilities.NetworkUtils;
import com.example.segd.itsalwayssunnysomewhere.utilities.OpenWeatherJsonUtils;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

	private TextView mWeatherTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);

		mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);

		loadWeatherData();
	}

	/**
	 * This method will get the user's preferred location for weather, and then tell some
	 * background method to get the weather data in the background.
	 */
	private void loadWeatherData() {
		String location = SunshinePreferences.getPreferredWeatherLocation(this);
		new FetchWeatherTask().execute(location);
	}

	public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

		// Create a class that extends AsyncTask to perform network requests
		// Override the doInBackground method to perform your network requests
		@Override
		protected String[] doInBackground(String... params) {

			/* If there's no zip code, there's nothing to look up. */
			if (params.length == 0) {
				return null;
			}

			String location = params[0];
			URL weatherRequestUrl = NetworkUtils.buildUrl(location);

			try {
				String jsonWeatherResponse = NetworkUtils
					.getResponseFromHttpUrl(weatherRequestUrl);

				String[] simpleJsonWeatherData = OpenWeatherJsonUtils
					.getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

				return simpleJsonWeatherData;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		// Override the onPostExecute method to display the results of the network request
		@Override
		protected void onPostExecute(String[] weatherData) {
			if (weatherData != null) {
				 /*
		         * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
				for (String weatherString : weatherData) {
					mWeatherTextView.append((weatherString) + "\n\n\n");
				}
			}
		}
	}
}