package com.udacity.stockhawk.ui;

import android.content.CursorLoader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.MyMarkerView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.AxisValueFormatter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockHistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		  OnChartGestureListener, OnChartValueSelectedListener
{
	@BindView(R.id.chart)
	LineChart mChart;
	private Uri stockUri;
	private XAxis xAxis;

	private final String TAG = getClass().getSimpleName();

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
		String[] mProjection = {Contract.Quote.COLUMN_HISTORY};

		return new CursorLoader(this,
				  stockUri,
				  mProjection,
				  null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		//TODO: setar os dados no gráfico

		ArrayList<Entry> values = new ArrayList<Entry>();

		if(cursor.moveToFirst())
		{
			String history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
			//Log.d(TAG, history);

			String historyInWeeks[] = history.split("\\r?\\n");

			long referenceTimeStamp = Long.valueOf(historyInWeeks[historyInWeeks.length - 1].split(",")[0]);

			// set a custom value formatter
			xAxis.setValueFormatter(new AxisValueFormatter(referenceTimeStamp, this));

			for(int i = historyInWeeks.length - 1; i >= 0; i--)
			{
				values.add(new Entry(Float.valueOf((Long.valueOf(historyInWeeks[i].split(",")[0]) - referenceTimeStamp)), Float.valueOf(historyInWeeks[i].split(",")[1])));
			}

			LineDataSet setStockQuotes = new LineDataSet(values, "Stock Quotes");

			setStockQuotes.setColor(ContextCompat.getColor(this, R.color.material_blue_A100));
			setStockQuotes.setCircleColor(ContextCompat.getColor(this, R.color.material_red_500));
			setStockQuotes.setLineWidth(1f);
			setStockQuotes.setCircleRadius(3f);
			setStockQuotes.setDrawCircleHole(false);
			setStockQuotes.setValueTextSize(9f);
			setStockQuotes.setValueTextColor(getColorOfAttribute(android.R.attr.textColorPrimary));
			setStockQuotes.setDrawFilled(true);
			setStockQuotes.setFormLineWidth(1f);
			setStockQuotes.setFillColor(Color.rgb(238, 247, 255));
			setStockQuotes.setMode(LineDataSet.Mode.CUBIC_BEZIER);

			ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
			dataSets.add(setStockQuotes); // add the datasets

			// create a data object with the datasets
			LineData data = new LineData(dataSets);

			data.setHighlightEnabled(true);

			// set data
			mChart.setData(data);
		}
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