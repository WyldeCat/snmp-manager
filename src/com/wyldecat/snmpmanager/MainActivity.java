// MainActivity.java

package com.wyldecat.snmpmanager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.wyldecat.snmpmanager.lib.SnmpManager;

public class MainActivity extends Activity {

  private SnmpManager snmpManager;
  private EditText editTextOID;
  private EditText editTextValue;
  private TextView textViewRes;

  final private Handler handler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
      textViewRes.append((String)msg.obj + "\n");
      super.handleMessage(msg);
    }
  };

  private String getStackTrace(Exception e) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(baos);
    e.printStackTrace(printStream);
    return baos.toString();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    snmpManager = new SnmpManager("kuwiden.iptime.org", 11161);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    editTextOID = (EditText)findViewById(R.id.edit_text_oid);
    editTextValue = (EditText)findViewById(R.id.edit_text_value);
    textViewRes = (TextView)findViewById(R.id.text_view_res);

    textViewRes.setMovementMethod(new ScrollingMovementMethod());
  }

  public void onGet(View view) {
    try {
      textViewRes.setText(
        snmpManager.Get(editTextOID.getText().toString()));
    } catch (Exception ignore) {
      Log.d("[snmp]", ignore.toString());
      Log.d("[snmp]", getStackTrace(ignore));
    }
  }

  public void onWalk(View view) {
    textViewRes.setText("");
    new Thread(new Runnable() {
      private SnmpManager snmpManager;
      private TextView textViewRes;

      public Runnable setup(SnmpManager snmpManager, TextView textViewRes) {
        this.snmpManager = snmpManager;
        this.textViewRes = textViewRes;

        return this;
      }
      public void run() {
        try {
          snmpManager.Walk(handler);
        } catch (Exception ignore) {
          Log.d("[snmp]", ignore.toString());
          Log.d("[snmp]", getStackTrace(ignore));
        }
      }
    }.setup(snmpManager, textViewRes)).start();
  }
}

