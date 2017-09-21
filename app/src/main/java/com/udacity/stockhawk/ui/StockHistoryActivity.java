package com.udacity.stockhawk.ui;

import android.content.CursorLoader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.MyMarkerView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.AxisValueFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockHistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		  OnChartGestureListener, OnChartValueSelectedListener
{
	@BindView(R.id.chart)
	LineChart mChart;
	@BindView(R.id.toolbarTitle)
	TextView toolbarTitle;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	private Uri stockUri;
	private XAxis xAxis;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_history);
		ButterKnife.bind(this);

		if (savedInstanceState == null)
		{
			stockUri = getIntent().getData();
			getLoaderManager().initLoader(0, null, this);
		}

		/*
		 * Faz com que uma toolbar seja a actionbar da activity
	    */
		setSupportActionBar(toolbar);

		/* Remove o nome do app da toolbar */
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		/* Mostra o back button no toolbar */
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mChart.setOnChartGestureListener(this);
		mChart.setOnChartValueSelectedListener(this);
		mChart.setDrawGridBackground(false);

		// no description text
		mChart.getDescription().setEnabled(false);

		// enable touch gestures
		mChart.setTouchEnabled(true);

		// enable scaling and dragging
		mChart.setDragEnabled(true);
		mChart.setScaleEnabled(true);

		// if disabled, scaling can be done on x- and y-axis separately
		mChart.setPinchZoom(true);
		mChart.animateX(3000);

		// create a custom MarkerView (extend MarkerView) and specify the layout
		// to use for it
		MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
		mv.setChartView(mChart); // For bounds control
		mChart.setMarker(mv); // Set the marker to the chart

		mChart.getAxisRight().setEnabled(false);
		mChart.getAxisLeft().setDrawGridLines(true);
		mChart.getAxisLeft().setTextColor(getColorOfAttribute(android.R.attr.textColorPrimary));
		mChart.getXAxis().setDrawGridLines(true);
		mChart.getLegend().setTextColor(getColorOfAttribute(android.R.attr.textColorSecondary));


		xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setTextSize(10f);
		xAxis.setTextColor(getColorOfAttribute(android.R.attr.textColorPrimary));
		xAxis.setDrawAxisLine(true);
		xAxis.setAxisLineColor(getColorOfAttribute(android.R.attr.textColorPrimary));
		xAxis.setDrawGridLines(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			/* Este é o id do back button */
			case android.R.id.home:
				onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public int getColorOfAttribute(int attribute)
	{
		/* pega o valor do atributo passado como parametro */
		TypedValue typedValue = new TypedValue();
		TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] {attribute});
		int color = a.getColor(0, 0);
		a.recycle();

		return color;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		String[] mProjection = {Contract.Quote.COLUMN_NAME, Contract.Quote.COLUMN_HISTORY};

		return new CursorLoader(this,
				  stockUri,
				  mProjection,
				  null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		//TODO: setar os dados no gráfico

		ArrayList<Entry> entries = new ArrayList<Entry>();
		ArrayList<String> xValues = new ArrayList<String>();

		if(cursor.moveToFirst())
		{
			toolbarTitle.setText(cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_NAME)));
			String history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

			String historyInWeeks[] = history.split("\\r?\\n");

			for(int i = 0; i < historyInWeeks.length; i++)
			{
				Log.d("StocksHistory", historyInWeeks[i]);
			}

			long referenceTimeStamp = Long.valueOf(historyInWeeks[historyInWeeks.length - 1].split(",")[0]);

			// set a custom value formatter
			//xAxis.setValueFormatter(new AxisValueFormatter(referenceTimeStamp, this));

			for(int i = 0; i < historyInWeeks.length; i++)
			{
				xValues.add(getDate(Long.valueOf(historyInWeeks[i].split(",")[0])));
				entries.add(new Entry(i/*Float.valueOf((referenceTimeStamp - Long.valueOf(historyInWeeks[i].split(",")[0])))*/,
						  Float.valueOf(historyInWeeks[i].split(",")[1])));
			}

			xAxis.setValueFormatter(new AxisValueFormatter(referenceTimeStamp, this, xValues));

			LineDataSet setStockQuotes = new LineDataSet(entries, "Stock Quotes");

			setStockQuotes.setColor(Color.WHITE);
			setStockQuotes.setCircleColor(Color.WHITE);
			setStockQuotes.setLineWidth(1f);
			setStockQuotes.setCircleRadius(3f);
			setStockQuotes.setDrawCircleHole(false);
			setStockQuotes.setValueTextSize(9f);
			setStockQuotes.setValueTextColor(getColorOfAttribute(android.R.attr.textColorPrimary));
			setStockQuotes.setDrawFilled(true);
			setStockQuotes.setFormLineWidth(1f);
			setStockQuotes.setFillColor(Color.rgb(238, 247, 255));
			setStockQuotes.setMode(LineDataSet.Mode.LINEAR);

			ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
			dataSets.add(setStockQuotes); // add the datasets

			// create a data object with the datasets
			LineData data = new LineData(dataSets);

			data.setHighlightEnabled(true);

			// set data
			mChart.setData(data);
			mChart.invalidate();
		}
	}

	/**
	 * Retorna a data de acordo com a linguagem do dispositivo
	 */
	private String getDate(long timestamp)
	{
		DateFormat mDataFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date mDate = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);

		try
		{
			mDate = mDataFormat.parse(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(calendar.get(Calendar.MONTH))
					  + "/" + String.valueOf(calendar.get(Calendar.YEAR)));
		}
		catch(ParseException e)
		{
			e.printStackTrace();
		}

		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		String s = dateFormat.format(mDate);

		return s;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	@Override
	public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
	{

	}

	@Override
	public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
	{
		// un-highlight values after the gesture is finished and no single-tap
		if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
			mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
	}

	@Override
	public void onChartLongPressed(MotionEvent me)
	{

	}

	@Override
	public void onChartDoubleTapped(MotionEvent me)
	{

	}

	@Override
	public void onChartSingleTapped(MotionEvent me)
	{

	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY)
	{

	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY)
	{

	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY)
	{

	}

	@Override
	public void onValueSelected(Entry e, Highlight h)
	{

	}

	@Override
	public void onNothingSelected()
	{

	}
}
