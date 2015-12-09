package com.wyx.tableview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.wyx.tableviewlib.TableBorderLayout;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

  SeekBar pgb;
  TableBorderLayout tableView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    pgb = (SeekBar) findViewById(R.id.pgb);
    tableView = (TableBorderLayout) findViewById(R.id.table);
    tableView.setRowColCount(5, 4);

    tableView.addView(getLayoutInflater().inflate(R.layout.cell, null));

    TextView test = null;
    for (int i = 0; i < 30; i++) {
      TextView textView = new TextView(this);
      if (i == 6) {
        test = textView;
      }
      if (i == 8) {
        textView.setTextSize(20);
      }
      if (i < 3) {
        textView.setGravity(Gravity.CENTER);
      }
      if (i == 3) {
        textView.setVisibility(View.GONE);
      }
      textView.setText("cell" + i);
      tableView.addView(textView);
    }
    tableView.eraseBorder(1, 1, TableBorderLayout.TOP);
    tableView.eraseBorder(1, 1, TableBorderLayout.LEFT);
    tableView.eraseBorder(1, 1, TableBorderLayout.RIGHT);
    tableView.eraseBorder(1, 1, TableBorderLayout.BOTTOM);
    //((RelativeLayout) findViewById(R.id.main)).addView(tableView);

    final TextView finalTest = test;
    pgb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tableView.setStrokeWidth(5 * progress);
        finalTest.setTextSize(5 * progress);
        tableView.setBorderColor(Color.parseColor("#" + getRandColorCode()));
        tableView.requestLayout();
        tableView.invalidate();
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  public static String getRandColorCode() {
    String r, g, b;
    Random random = new Random();
    r = Integer.toHexString(random.nextInt(256)).toUpperCase();
    g = Integer.toHexString(random.nextInt(256)).toUpperCase();
    b = Integer.toHexString(random.nextInt(256)).toUpperCase();

    r = r.length() == 1 ? "0" + r : r;
    g = g.length() == 1 ? "0" + g : g;
    b = b.length() == 1 ? "0" + b : b;
    return r + g + b;
  }
}
