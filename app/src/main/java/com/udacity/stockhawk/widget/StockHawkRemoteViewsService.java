package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by vinicius on 21/04/17.
 */

/**
 * Classe usada para exibir coleções de dados remotos como os de um content provider.
 * Os dados providos pelo RemoteViewsService são apresentados no widget utilizando
 * um dos tipos de view abaixo listados, aos quais nos referimos como "collection views":
 *
 * ListView
 * GridView
 * StackView
 * AdapterViewFlipper
 *
 * Como dito antes essas "collection views" exibem coleções de dados remotos. Isso significa
 * que elas usam um Adapter para ligar sua interface aos dados. Um Adapter liga um item individual de
 * um conjunto de dados em um View object individual. No widget o Adapter é substituido por um
 * RemoteViewsFactory que é simplesmente um invólucro em torno da interface do Adapter. Quando requisitado
 * por um item especifico da colecao, o RemoteViewsFactory cria e retorna o item para a coleção como
 * um RemoteViews object. Para incluir uma "collection view" no widget, deve ser implementado
 * RemoteViewsService e RemoteViewsFactory
 *
 * RemoteViewsService é um serviço que permite um adaptador remoto requisitar RemoteViews objects
 */
public class StockHawkRemoteViewsService extends RemoteViewsService
{
	private final String TAG = getClass().getSimpleName();
	private final DecimalFormat dollarFormatWithPlus;
	private final DecimalFormat dollarFormat;
	private final DecimalFormat percentageFormat;
	private Context context;

	public StockHawkRemoteViewsService()
	{
		dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
		dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
		dollarFormatWithPlus.setPositivePrefix("+$");
		percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
		percentageFormat.setMaximumFractionDigits(2);
		percentageFormat.setMinimumFractionDigits(2);
		percentageFormat.setPositivePrefix("+");
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		context = this.getApplicationContext();

		/* É uma interface para um Adapter entre uma collection view
		(como ListView, GridView e assim por diante) e os dados para essa view.
		Essa implementação é responsável por criar objetos RemoteViews para cada item do conjunto de dados */
		return new RemoteViewsFactory()
		{
			private final String TAGRemoteViewsFactory = "RemoteViewsFactory";
			private Cursor data = null;

			@Override
			public void onCreate()
			{

			}

			/* Este método é chamado quando eu chamo appWidgetManager.notifyAppWidgetViewDataChanged dentro
			* do método onReceive da classe StockHawkWidgetProvider*/
			@Override
			public void onDataSetChanged()
			{
				if (data != null)
				{
					data.close();
				}

				final long identityToken = Binder.clearCallingIdentity();
				data = getContentResolver().query(Contract.Quote.URI,
						  Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
						  null, null, Contract.Quote.COLUMN_SYMBOL);
				Binder.restoreCallingIdentity(identityToken);
			}

			@Override
			public void onDestroy()
			{
				if (data != null) {
					data.close();
					data = null;
				}
			}

			@Override
			public int getCount()
			{
				return data == null ? 0 : data.getCount();
			}

			@Override
			public RemoteViews getViewAt(int position)
			{
				if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position))
				{
					return null;
				}

				RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote_widget);

				views.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
				views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

				float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
				float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

				// TODO: ver como adicionar um shape em uma remote view
				if (rawAbsoluteChange > 0) {
					views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
				} else {
					views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
				}

				String change = dollarFormatWithPlus.format(rawAbsoluteChange);
				String percentage = percentageFormat.format(percentageChange / 100);

				if (PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key))) {
					views.setTextViewText(R.id.change, change);
				} else {
					views.setTextViewText(R.id.change, percentage);
				}

				final Intent fillInIntent = new Intent();
				Uri quoteUri = Contract.Quote.makeUriForStock(data.getString(Contract.Quote.POSITION_SYMBOL));
				fillInIntent.setData(quoteUri);
				views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

				return views;
			}

			@Override
			public RemoteViews getLoadingView()
			{
				return null;
			}

			@Override
			public int getViewTypeCount()
			{
				return 1;
			}

			@Override
			public long getItemId(int position)
			{
				if (data.moveToPosition(position))
					return data.getLong(0);
				return position;
			}

			@Override
			public boolean hasStableIds()
			{
				return true;
			}
		};
	}
}
