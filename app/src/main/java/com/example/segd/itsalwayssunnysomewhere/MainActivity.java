
package com.example.segd.itsalwayssunnysomewhere;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.segd.itsalwayssunnysomewhere.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.example.segd.itsalwayssunnysomewhere.R;
import com.example.segd.itsalwayssunnysomewhere.data.SunshinePreferences;
import com.example.segd.itsalwayssunnysomewhere.utilities.NetworkUtils;
import com.example.segd.itsalwayssunnysomewhere.utilities.OpenWeatherJsonUtils;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler {

	private static final String TAG = MainActivity.class.getSimpleName();

	private RecyclerView mRecyclerView;
	private ForecastAdapter mForecastAdapter;
	private TextView mErrorMessageDisplay;
	private ProgressBar mLoadingIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);

		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
		/* This TextView is used to display errors and will be hidden if there are no errors */
		mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
		mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

		/*
		 * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
		LinearLayoutManager layoutManager
			= new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(layoutManager);

		/*
		 * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
		mRecyclerView.setHasFixedSize(true);

		/*
		 * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
		mForecastAdapter = new ForecastAdapter(this);

		/* Setting the adapter attaches it to the RecyclerView in our layout. */
		mRecyclerView.setAdapter(mForecastAdapter);

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

	/**
	 * This method is overridden by our MainActivity class in order to handle RecyclerView item
	 * clicks.
	 *
	 * @param weatherForDay The weather for the day that was clicked
	 */
	@Override
	public void onClick(String weatherForDay) {
		Context context = this;
		Class destinationClass = DetailActivity.class;
		Intent intentToStartDetailActivity = new Intent(context, destinationClass);
		intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
		startActivity(intentToStartDetailActivity);
	}

	private void showWeatherDataView() {
		/* First, make sure the error is invisible */
		mErrorMessageDisplay.setVisibility(View.INVISIBLE);
		/* Then, make sure the weather data is visible */
		mRecyclerView.setVisibility(View.VISIBLE);
	}

	private void showErrorMessage() {
		/* First, hide the currently visible data */
		mRecyclerView.setVisibility(View.INVISIBLE);
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
				mForecastAdapter.setWeatherData(weatherData);
			} else {
				//If the weather data was null, show the error message
				showErrorMessage();
			}
		}
	}


	/**
	 * This method uses the URI scheme for showing a location found on a
	 * map. This super-handy intent is detailed in the "Common Intents"
	 * page of Android's developer site:
	 *
	 * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
	 *
	 * Hint: Hold Command on Mac or Control on Windows and click that link to automagically open the
	 * Common Intents page
	 */
	private void openLocationInMap() {
		String addressString = "1600 Ampitheatre Parkway, CA";
		Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geoLocation);

		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			Log.d(TAG, "Couldn't call " + geoLocation.toString()
				+ ", no receiving apps installed!");
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
			mForecastAdapter.setWeatherData(null);
			loadWeatherData();
			return true;
		}
//Launch the map when the map menu item is clicked
		if (id == R.id.action_map) {
			openLocationInMap();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}