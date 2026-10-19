package es.topopepo.relojgordo;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.widget.FrameLayout;

import java.util.Random;

public class ClockDream extends DreamService {

    private final Handler h = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();
    private Clock clock;
    private int tick;

    private final Runnable loop = new Runnable() {
        @Override public void run() {
            clock.refresh();
            // Cada 5 min se desplaza un poco para no quemar el OLED
            if (++tick % 5 == 0) shift();
            long now = System.currentTimeMillis();
            h.postDelayed(this, 60_000 - (now % 60_000) + 50);
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(!Clock.prefs(this).getBoolean(Clock.K_DIM, true));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        clock = new Clock(this);
        int m = clock.fill ? FrameLayout.LayoutParams.MATCH_PARENT : FrameLayout.LayoutParams.WRAP_CONTENT;
        if (clock.fill) {
            int pad = Math.round(32 * getResources().getDisplayMetrics().density);
            root.setPadding(pad, pad, pad, pad);
        }
        root.addView(clock, new FrameLayout.LayoutParams(m, m, Gravity.CENTER));
        setContentView(root);
    }

    private void shift() {
        float d = getResources().getDisplayMetrics().density * (clock.fill ? 0.5f : 1f);
        clock.animate()
                .translationX((rnd.nextFloat() * 2 - 1) * 24 * d)
                .translationY((rnd.nextFloat() * 2 - 1) * 40 * d)
                .setDuration(1500).start();
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        h.removeCallbacks(loop);
        h.post(loop);
    }

    @Override
    public void onDreamingStopped() {
        h.removeCallbacks(loop);
        super.onDreamingStopped();
    }
}
