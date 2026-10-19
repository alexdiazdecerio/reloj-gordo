package es.topopepo.relojgordo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private Clock preview;
    private SharedPreferences p;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = Clock.prefs(this);
        int pad = dp(20);

        ScrollView scroll = new ScrollView(this);
        scroll.setFitsSystemWindows(true);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(pad, pad, pad, pad);
        scroll.addView(col);

        TextView title = new TextView(this);
        title.setText("Reloj Gordo");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        col.addView(title);

        // Vista previa
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(Color.BLACK);
        box.setPadding(0, dp(28), 0, dp(28));
        preview = new Clock(this);
        box.addView(preview);
        previewLayout();
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, dp(16), 0, dp(16));
        col.addView(box, blp);

        toggle(col, "Llenar pantalla (tamaño automático)", Clock.K_FILL, false);
        slider(col, "Tamaño de la hora (sin llenar)", Clock.K_TIME, Clock.DEF_TIME, 60, 260);
        slider(col, "Tamaño del día", Clock.K_DATE, Clock.DEF_DATE, 14, 72);
        toggle(col, "Mostrar el día", Clock.K_SHOW_DATE, true);
        toggle(col, "Atenuar pantalla (ahorra batería y OLED)", Clock.K_DIM, true);

        TextView help = new TextView(this);
        help.setText("\nPara que salte al cargar: Ajustes → Pantalla → Salvapantallas → elige «Reloj Gordo» "
                + "y en «Cuándo empezar» marca «Mientras se carga».");
        help.setTextColor(0xFFAAAAAA);
        col.addView(help);

        Button open = new Button(this);
        open.setText("Abrir ajustes de salvapantallas");
        open.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS)); }
            catch (Exception e) { startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)); }
        });
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        olp.setMargins(0, dp(16), 0, 0);
        col.addView(open, olp);

        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preview.refresh();
    }

    private void slider(LinearLayout col, String label, String key, int def, int min, int max) {
        TextView t = new TextView(this);
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);
        t.setPadding(0, dp(12), 0, 0);
        col.addView(t);
        SeekBar s = new SeekBar(this);
        s.setMax(max - min);
        int cur = p.getInt(key, def);
        s.setProgress(cur - min);
        t.setText(label + ": " + cur);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int v, boolean user) {
                int val = v + min;
                t.setText(label + ": " + val);
                p.edit().putInt(key, val).apply();
                preview.applyPrefs();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        col.addView(s);
    }

    private void toggle(LinearLayout col, String label, String key, boolean def) {
        Switch sw = new Switch(this);
        sw.setText(label);
        sw.setTextColor(Color.WHITE);
        sw.setTextSize(16);
        sw.setPadding(0, dp(12), 0, dp(4));
        sw.setChecked(p.getBoolean(key, def));
        sw.setOnCheckedChangeListener((v, on) -> {
            p.edit().putBoolean(key, on).apply();
            preview.applyPrefs();
            previewLayout();
        });
        col.addView(sw);
    }

    private void previewLayout() {
        boolean fill = p.getBoolean(Clock.K_FILL, false);
        preview.setLayoutParams(fill
                ? new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(420), Gravity.CENTER)
                : new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
