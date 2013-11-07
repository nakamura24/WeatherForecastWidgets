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

import java.util.ArrayList;

import jp.library.weatherforecast.StaticHash;
import jp.library.weatherforecast.WeatherForecast;
import jp.library.weatherforecast.WeatherForecast.*;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import static jp.widget.weatherforecasts.Constant.*;

public class Widget2days extends WidgetBase {
	public static final String TAG = "Widget2days";
	private WeatherForecast mWeatherForecast;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i(TAG, "onEnabled");
		try {
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onUpdate(final Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "onUpdate");
		try {
			mWeatherForecast = new WeatherForecast();
			StaticHash hash = new StaticHash(context);
			for (int i = 0; i < appWidgetIds.length; i++) {
				final int appWidgetId = appWidgetIds[i];
				int id = hash.get(LOCATEID,
						String.valueOf(appWidgetId), INIT_ID);
				mWeatherForecast.getForecast(context, id);
				mWeatherForecast.setOnPostExecute(new OnPostExecute() {
					@Override
					public void onPostExecute() {
						updateAppWidget(context, appWidgetId);
					}
				});
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(TAG, "onDeleted");
		super.onDeleted(context, appWidgetIds);
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d(TAG, "onDeleted - " + String.valueOf(appWidgetIds[i]));
				hash.remove(LOCATEID,
						String.valueOf(appWidgetIds[i]));
				hash.remove(POSITION,
						String.valueOf(appWidgetIds[i]));
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		super.onDisabled(context);
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			ArrayList<String> appWidgetIds = hash
					.keys(LOCATEID);
			for (int i = 0; i < appWidgetIds.size(); i++) {
				Log.d(TAG, "onDeleted - " + appWidgetIds);
				hash.remove(LOCATEID, appWidgetIds.get(i));
				hash.remove(POSITION, appWidgetIds.get(i));
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i(TAG, "onReceive - " + intent.getAction());
		try {
			if (CONFIG_DONE.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					final int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					int id = extras.getInt(LOCATEID, INIT_ID);
					Log.d(TAG,
							"CONFIG_DONE appWidgetId="
									+ String.valueOf(appWidgetId) + "id="
									+ String.valueOf(id));
					mWeatherForecast = new WeatherForecast();
					mWeatherForecast.getForecast(context, id);
					mWeatherForecast.setOnPostExecute(new OnPostExecute() {
						@Override
						public void onPostExecute() {
							updateAppWidget(context, appWidgetId);
						}
					});
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	public void updateAppWidget(Context context, int appWidgetId) {
		try {
			Log.i(TAG, "updateAppWidget - " + String.valueOf(appWidgetId));
			// ボタンが押された時に発行されるインテントを準備する
			Intent intent = new Intent(context, WidgetConfigure.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.putExtra(APPWIDGET_CALLER, TAG);
			intent.setAction(APPWIDGET_CONFIGURE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					appWidgetId, intent, 0);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_2days);
			remoteViews.setOnClickPendingIntent(R.id.relativeLayout_2days,
					pendingIntent);

			StaticHash hash = new StaticHash(context);
			int id = hash.get(LOCATEID,
					String.valueOf(appWidgetId), INIT_ID);

			mWeatherForecast = new WeatherForecast();
			remoteViews.setTextViewText(R.id.textView_location,
					mWeatherForecast.getLocationName(id));
			ArrayList<WeeklyForecast> weeklyForecasts = mWeatherForecast
					.getWeeklyForecast(context, id);
			if (weeklyForecasts == null || weeklyForecasts.size() == 0)
				return;
			remoteViews.setTextViewText(R.id.textView_date1, weeklyForecasts.get(0).Date);
			remoteViews.setImageViewResource(R.id.imageView1,
					mWeatherForecast.getBitmapResource(weeklyForecasts.get(0).Forecast));
			remoteViews.setTextViewText(R.id.textView_max1,
					weeklyForecasts.get(0).MaxTemp);
			remoteViews.setTextViewText(R.id.textView_min1,
					weeklyForecasts.get(0).MinTemp);
			remoteViews.setTextViewText(R.id.textView_probability1,
					weeklyForecasts.get(0).Probability);
			remoteViews.setTextViewText(R.id.textView_date2, weeklyForecasts.get(1).Date);
			remoteViews.setImageViewResource(R.id.imageView2,
					mWeatherForecast.getBitmapResource(weeklyForecasts.get(1).Forecast));
			remoteViews.setTextViewText(R.id.textView_max2,
					weeklyForecasts.get(1).MaxTemp);
			remoteViews.setTextViewText(R.id.textView_min2,
					weeklyForecasts.get(1).MinTemp);
			remoteViews.setTextViewText(R.id.textView_probability2,
					weeklyForecasts.get(1).Probability);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}
}
