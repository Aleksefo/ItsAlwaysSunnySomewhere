
package com.example.segd.itsalwayssunnysomewhere;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.segd.itsalwayssunnysomewhere.R;
import com.example.segd.itsalwayssunnysomewhere.data.SunshinePreferences;
import com.example.segd.itsalwayssunnysomewhere.utilities.NetworkUtils;
import com.example.segd.itsalwayssunnysomewhere.utilities.OpenWeatherJsonUtils;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

	private TextView mWeatherTextView;
	private TextView mErrorMessageDisplay;
	private ProgressBar mLoadingIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);

		mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);
		/* This TextView is used to display errors and will be hidden if there are no errors */
		mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
		mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
		loadWeatherData();
	}

	/**
	 * This method will get the user's preferred location for weather, and then tell some
	 * background method to get the weather data in the background.
	 */
	private void loadWeatherData() {
		showWeatherDataView();

		String location = SunshinePreferences.getPreferredWeatherLocation(this);
		new FetchWeatherTask().execute(location);
	}

	private void showWeatherDataView() {
		/* First, make sure the error is invisible */
		mErrorMessageDisplay.setVisibility(View.INVISIBLE);
		/* Then, make sure the weather data is visible */
		mWeatherTextView.setVisibility(View.VISIBLE);
	}

	private void showErrorMessage() {
		/* First, hide the currently visible data */
		mWeatherTextView.setVisibility(View.INVISIBLE);
		/* Then, show the error */
		mErrorMessageDisplay.setVisibility(View.VISIBLE);
	}

	public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

		// override the method onPreExecute and show the loading indicator
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingIndicator.setVisibility(View.VISIBLE);
		}

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
			mLoadingIndicator.setVisibility(View.INVISIBLE);
			if (weatherData != null) {
				//If the weather data was not null, make sure the data view is visible
				showWeatherDataView();
				 /*
				 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
				for (String weatherString : weatherData) {
					mWeatherTextView.append((weatherString) + "\n\n\n");
				}
			} else {
				//If the weather data was null, show the error message
				showErrorMessage();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
		MenuInflater inflater = getMenuInflater();
		        /* Use the inflater's inflate method to inflate our menu layout to this menu */
		inflater.inflate(R.menu.forecast, menu);
		        /* Return true so that the menu is displayed in the Toolbar */
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_refresh) {
			mWeatherTextView.setText("");
			loadWeatherData();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}