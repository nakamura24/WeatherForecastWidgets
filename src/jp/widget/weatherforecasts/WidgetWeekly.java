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
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import static jp.widget.weatherforecasts.Constant.*;

public class WidgetWeekly extends WidgetBase {
	public static final String TAG = "WidgetWeekly";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i(TAG, "onEnabled");
		try {
			Intent intent = new Intent(context, WidgetService.class);
			context.startService(intent);
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
			final WeatherForecast weatherForecast = new WeatherForecast();
			StaticHash hash = new StaticHash(context);
			for (int i = 0; i < appWidgetIds.length; i++) {
				final int appWidgetId = appWidgetIds[i];
				int id = hash.get(LOCATEID + TAG, String.valueOf(appWidgetId),
						INIT_ID);
				hash.put(LOCATEID + TAG, String.valueOf(appWidgetId), id);
				weatherForecast.setOnPostExecute(new OnPostExecute() {
					@Override
					public void onPostExecute() {
						updateAppWidget(context, appWidgetId, weatherForecast);
					}
				});
				weatherForecast.getForecast(context, id);
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
				hash.remove(LOCATEID + TAG, String.valueOf(appWidgetIds[i]));
				hash.remove(POSITION, String.valueOf(appWidgetIds[i]));
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
			ArrayList<String> appWidgetIds = hash.keys(LOCATEID + TAG);
			for (int i = 0; i < appWidgetIds.size(); i++) {
				Log.d(TAG, "onDeleted - " + appWidgetIds);
				hash.remove(LOCATEID + TAG, appWidgetIds.get(i));
				hash.remove(POSITION, appWidgetIds.get(i));
			}
			Intent intent = new Intent(context, WidgetService.class);
			context.stopService(intent);
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
					StaticHash hash = new StaticHash(context);
					int id = extras.getInt(LOCATEID, hash.get(LOCATEID + TAG, INIT_ID));
					hash.put(LOCATEID + TAG, String.valueOf(appWidgetId), id);
					Log.d(TAG,
							"CONFIG_DONE appWidgetId="
									+ String.valueOf(appWidgetId) + "id="
									+ String.valueOf(id));
					final WeatherForecast weatherForecast = new WeatherForecast();
					weatherForecast.setOnPostExecute(new OnPostExecute() {
						@Override
						public void onPostExecute() {
							updateAppWidget(context, appWidgetId,
									weatherForecast);
						}
					});
					weatherForecast.getForecast(context, id);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	public static void updateAppWidget(Context context, int appWidgetId,
			WeatherForecast weatherForecast) {
		try {
			Log.i(TAG, "updateAppWidget - " + String.valueOf(appWidgetId));
			// ボタンが押された時に発行されるインテントを準備する
			Intent intent = new Intent(context, WidgetWeeklyConfig.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setAction(APPWIDGET_CONFIGURE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					appWidgetId, intent, 0);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_weekly);
			remoteViews.setOnClickPendingIntent(R.id.relativeLayout_weekly,
					pendingIntent);

			StaticHash hash = new StaticHash(context);
			int id = hash.get(LOCATEID + TAG, String.valueOf(appWidgetId),
					INIT_ID);

			weatherForecast = new WeatherForecast();
			remoteViews.setTextViewText(R.id.textView_location,
					weatherForecast.getLocationName(id));
			ArrayList<WeeklyForecast> weeklyForecasts = weatherForecast
					.getWeeklyForecast(context, id);
			if (weeklyForecasts == null || weeklyForecasts.size() == 0)
				return;
			int[] textView_dates = { R.id.textView_date1, R.id.textView_date2,
					R.id.textView_date3, R.id.textView_date4,
					R.id.textView_date5, R.id.textView_date6,
					R.id.textView_date7, R.id.textView_date8, };
			int[] textView_temps = { R.id.textView_temp1, R.id.textView_temp2,
					R.id.textView_temp3, R.id.textView_temp4,
					R.id.textView_temp5, R.id.textView_temp6,
					R.id.textView_temp7, R.id.textView_temp8, };
			int[] textView_probabilitys = { R.id.textView_probability1, R.id.textView_probability2,
					R.id.textView_probability3, R.id.textView_probability4,
					R.id.textView_probability5, R.id.textView_probability6,
					R.id.textView_probability7, R.id.textView_probability8, };
			int[] imageViews = { R.id.imageView1, R.id.imageView2,
					R.id.imageView3, R.id.imageView4, R.id.imageView5,
					R.id.imageView6, R.id.imageView7, R.id.imageView8, };
			for (int i = 0; i < weeklyForecasts.size() && i < 8; i++) {
				remoteViews.setTextViewText(textView_dates[i],
						weeklyForecasts.get(i).Date);
				remoteViews.setTextViewText(textView_temps[i],
						weeklyForecasts.get(i).Temp);
				remoteViews.setTextViewText(textView_probabilitys[i],
						weeklyForecasts.get(i).Probability);
				remoteViews.setImageViewResource(imageViews[i], weatherForecast
						.getBitmapResource(weeklyForecasts.get(i).Forecast));
			}

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	public static class WidgetService extends Service {
		@Override
		public IBinder onBind(Intent in) {
			return null;
		}

		@Override
		public void onCreate() {
			super.onCreate();
			Log.i(TAG, "onCreate");
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_USER_PRESENT);
			registerReceiver(mReceiver, filter);
		}

		@Override
		public void onDestroy() {
			Log.i(TAG, "onDestroy");
			unregisterReceiver(mReceiver);
			super.onDestroy();
		}

		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, Intent intent) {
				Log.i(TAG, "mReceiver onReceive = " + intent.getAction());
				if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
					StaticHash hash = new StaticHash(context);
					ArrayList<String> appWidgetIds = hash.keys(LOCATEID + TAG);
					for (int i = 0; i < appWidgetIds.size(); i++) {
						final int appWidgetId = Integer.parseInt(appWidgetIds
								.get(i));
						int id = hash.get(LOCATEID + TAG,
								String.valueOf(appWidgetId), INIT_ID);
						final WeatherForecast weatherForecast = new WeatherForecast();
						weatherForecast.setOnPostExecute(new OnPostExecute() {
							@Override
							public void onPostExecute() {
								updateAppWidget(context, appWidgetId,
										weatherForecast);
							}
						});
						weatherForecast.getForecast(context, id);
					}
				}
			}
		};
	}
}
