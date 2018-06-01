// MainActivity.java

package com.wyldecat.snmpmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wyldecat.snmpmanager.lib.SnmpManager;

public class MainActivity extends Activity {

  SnmpManager snmpManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    snmpManager = new SnmpManager("kuwiden.iptime.org", 11161);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onGet(View view) {
    snmpManager.Get("");
  }
}

