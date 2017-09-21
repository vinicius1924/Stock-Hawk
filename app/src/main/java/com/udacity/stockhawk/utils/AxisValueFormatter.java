package com.udacity.stockhawk.utils;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yasir on 02/06/16.
 */
public class AxisValueFormatter implements IAxisValueFormatter
{

    private long referenceTimestamp; // timestamp minimo do conjunto de dados
    private DateFormat mDataFormat;
    private Date mDate;
    private Context context;
    ArrayList<String> xValues = new ArrayList<String>();


    public AxisValueFormatter(long referenceTimestamp, Context context, ArrayList<String> xValues) {
        this.referenceTimestamp = referenceTimestamp;
        this.mDataFormat = new SimpleDateFormat("dd/MM/yyyy");
        this.mDate = new Date();
        this.context = context;
        this.xValues = xValues;
    }


    /**
     * Chamado quando um valor do eixo x está para ser formatado antes de ser desenhado.
     * Por questões de performance, evite calculos excessivos e alocacao de memoria
     * dentro deste metodo.
     *
     * @param value o valor a ser formatado
     * @param axis  o eixo ao qual o valor pertence
     * @return
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis)
    {
        return xValues.get((int) value % xValues.size());
//        long convertedTimestamp = (long) value;
//
//        // Pega o timestamp original
//        long originalTimestamp = referenceTimestamp - convertedTimestamp;
//
//        // Converte timestamp para hora:minuto
//        //return getHour(originalTimestamp);
//
//        // Converte timestamp para dia/mes/ano
//        return getDate(originalTimestamp);
    }

    private String getHour(long timestamp){
        try{
            mDate.setTime(timestamp*1000);
            return mDataFormat.format(mDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    /**
     * Retorna a data de acordo com a linguagem do dispositivo
     */
    private String getDate(long timestamp){

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

        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        String s = dateFormat.format(mDate);

        return s;
    }
}