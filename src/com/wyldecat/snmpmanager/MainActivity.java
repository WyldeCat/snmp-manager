// MainActivity.java

package com.wyldecat.snmpmanager;

import com.wyldecat.snmpmanager.lib.SnmpManager;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    SnmpManager snmpManager = new SnmpManager("kuwiden.iptime.org", 11161);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }
}

