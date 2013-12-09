/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.widget.weatherforecasts;

import jp.library.weatherforecast.StaticHash;
import jp.library.weatherforecast.WeatherForecast;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout.LayoutParams;

import static jp.widget.weatherforecasts.Constant.*;

public class WidgetWeeklyConfig extends Activity {
	private static final String TAG = "WidgetWeeklyConfig";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private int mPosition = 63 - 1;
	private int mId = 4410;
	private WeatherForecast mWeatherForecast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		try {
			// setResult(RESULT_CANCELED);
			// AppWidgetID の取得
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				Log.d(TAG, "mAppWidgetId=" + String.valueOf(mAppWidgetId));
				StaticHash hash = new StaticHash(this);
				mId = hash.get(LOCATEID, String.valueOf(mAppWidgetId), mId);
				mPosition = hash.get(POSITION, String.valueOf(mAppWidgetId),
						mPosition);
			}
			mWeatherForecast = new WeatherForecast();

			setContentView(R.layout.widget_configure);
			getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			ArrayAdapter<String> forecast_location_adapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item);
			forecast_location_adapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// アイテムを追加します
			for (int i = 0; i < mWeatherForecast.getLocationIDs().length; i++) {
				forecast_location_adapter.add(mWeatherForecast
						.getLocationName(mWeatherForecast.getLocationIDs()[i]));
			}
			Spinner spinner1 = (Spinner) findViewById(R.id.spinner_pref);
			// アダプターを設定します
			spinner1.setAdapter(forecast_location_adapter);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					mPosition = spinner.getSelectedItemPosition();
					mId = mWeatherForecast.getLocationIDs()[mPosition];
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					mPosition = 63 - 1;
					mId = 4410;
				}
			});
			spinner1.setSelection(mPosition);
			Log.i(TAG, "onCreate end");
		} catch (Exception e) {
		}
	}

	// Button の onClick で実装
	public void onOKButtonClick(View v) {
		try {
			Log.i(TAG, "onOKButtonClick");
			StaticHash hash = new StaticHash(this);
			hash.put(POSITION, String.valueOf(mAppWidgetId), mPosition);
			Intent intent = new Intent(this, WidgetWeekly.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.putExtra(LOCATEID, mId);
			intent.setAction(CONFIG_DONE);
			sendBroadcast(intent);
			setResult(RESULT_OK, intent);
			finish();
			Log.i(TAG, "onOKButtonClick End");
		} catch (Exception e) {
		}
	}
}
