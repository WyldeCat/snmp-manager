// MainActivity.java

package com.wyldecat.snmpmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

import com.wyldecat.snmpmanager.lib.SnmpManager;

public class MainActivity extends Activity {

  SnmpManager snmpManager;
  EditText editTextOID;
  EditText editTextValue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    snmpManager = new SnmpManager("kuwiden.iptime.org", 11161);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    editTextOID = (EditText)findViewById(R.id.edit_text_oid);
    editTextValue = (EditText)findViewById(R.id.edit_text_value);
  }

  public void onGet(View view) {
    try {
      snmpManager.Get(editTextOID.getText().toString());
    } catch (Exception ignore) { }
  }
}

