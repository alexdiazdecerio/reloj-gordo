package es.topopepo.relojgordo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Hora + día en grande. Lo usan el salvapantallas y la vista previa de ajustes. */
public class Clock extends LinearLayout {

    static final String PREFS = "reloj";
    static final String K_TIME = "time_sp", K_DATE = "date_sp", K_DIM = "dim", K_SHOW_DATE = "show_date", K_FILL = "fill";
    static final int DEF_TIME = 120, DEF_DATE = 28;

    final TextView time, date;

    public Clock(Context c) {
        super(c);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        time = new TextView(c);
        time.setTextColor(Color.WHITE);
        time.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        time.setIncludeFontPadding(false);
        time.setFontFeatureSettings("tnum"); // dígitos de ancho fijo, no baila al cambiar
        time.setGravity(Gravity.CENTER);

        date = new TextView(c);
        date.setTextColor(0xFFB0B0B0);
        date.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        date.setGravity(Gravity.CENTER);
        date.setLetterSpacing(0.04f);

        addView(time);
        addView(date);
        applyPrefs();
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    boolean fill;

    void applyPrefs() {
        SharedPreferences p = prefs(getContext());
        fill = p.getBoolean(K_FILL, false);
        if (fill) {
            // Ocupa todo el hueco libre y el texto se escala solo al máximo que quepa
            time.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
            time.setLineSpacing(0, 0.82f);
            time.setAutoSizeTextTypeUniformWithConfiguration(40, 4000, 4, TypedValue.COMPLEX_UNIT_PX);
        } else {
            time.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
            time.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            time.setLineSpacing(0, 1f);
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, p.getInt(K_TIME, DEF_TIME));
        }
        refresh();
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, p.getInt(K_DATE, DEF_DATE));
        date.setVisibility(p.getBoolean(K_SHOW_DATE, true) ? VISIBLE : GONE);
    }

    void refresh() {
        Locale loc = Locale.getDefault();
        Date now = new Date();
        String h = DateFormat.is24HourFormat(getContext()) ? "HH" : "h";
        boolean portrait = getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_LANDSCAPE;
        // En vertical y modo llenar: horas encima de minutos, como StandBy
        String tp = (fill && portrait) ? h + "\n" + "mm" : h + ":mm";
        time.setText(new SimpleDateFormat(tp, loc).format(now));
        String dp = DateFormat.getBestDateTimePattern(loc, "EEEEdMMMM");
        String d = new SimpleDateFormat(dp, loc).format(now);
        if (!d.isEmpty()) d = d.substring(0, 1).toUpperCase(loc) + d.substring(1);
        date.setText(d);
    }
}
