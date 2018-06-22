// MainActivity.java

package com.wyldecat.snmpmanager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
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
  private TextView textViewTo;

  /*
   * Handler receive messages and update UI with them
   */
  final private Handler handler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
      int scrollAmount;

      if (msg.obj instanceof SocketTimeoutException) {
        textViewTo.setText("Timeout has occurred!\n");
        return;
      }

      ((String)msg.obj).replace(" ", "\u00A0");
      textViewRes.append((String)msg.obj + "\n");
      scrollAmount =
        textViewRes.getLayout().getLineTop(textViewRes.getLineCount()) -
        textViewRes.getHeight();
      if (scrollAmount > 0) {
        textViewRes.scrollTo(0, scrollAmount);
      } else {
        textViewRes.scrollTo(0, 0);
      }

      super.handleMessage(msg);
    }
  };

  /*
   * A function stringify stacktrace
   */
  private String getStackTrace(Exception e) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(baos);
    e.printStackTrace(printStream);
    return baos.toString();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // create thread and init snmpManager
    Thread initTh = new Thread() {
      @Override
      public void run() {
        snmpManager = new SnmpManager("kuwiden.iptime.org", 11161);
      }
    };

    initTh.start();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    editTextOID = (EditText)findViewById(R.id.edit_text_oid);
    editTextValue = (EditText)findViewById(R.id.edit_text_value);
    textViewRes = (TextView)findViewById(R.id.text_view_res);
    textViewTo = (TextView)findViewById(R.id.text_view_to);

    textViewRes.setMovementMethod(new ScrollingMovementMethod());

    try {
      initTh.join();
    } catch(Exception e) {
      Log.d("[snmp]", getStackTrace(e));
      textViewRes.append("Critical error!\n");
    }
  }

  /*
   * GET Button's listener
   */
  public void onGet(View view) {
    call(0xa0, editTextOID.getText().toString(), null);
  }

  /*
   * WALK Button's listener
   */
  public void onWalk(View view) {
    call(0xa1, null, null);
  }

  /*
   * SET Button's listener
   */
  public void onSet(View view) {
    call(0xa3,
      editTextOID.getText().toString(),
      editTextValue.getText().toString());
  }

  /*
   * call() create new thread and call appropriate snmpManager's function.
   * check whether snmpManager is busy or not
   */
  private void call(final int type, final String oid, final String val) {
    if (snmpManager.isWorking()) return;

    textViewTo.setText("");
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
          snmpManager.setIsWorking();
          switch (type) {
          case 0xa0:
            Log.d("[snmp]", "Get");
            snmpManager.Get(handler, oid);
            break;
          case 0xa1:
            Log.d("[snmp]", "Walk");
            snmpManager.Walk(handler);
            break;
          case 0xa3:
            Log.d("[snmp]", "Set");
            snmpManager.Set(handler, oid, val);
            break;
          }
        } catch (Exception ignore) {
          if (ignore instanceof SocketTimeoutException) {
            android.os.Message msg;

            Log.d("[snmp]", ignore.toString());
            Log.d("[snmp]", getStackTrace(ignore));

            msg = handler.obtainMessage();
            msg.obj = ignore;
            handler.sendMessage(msg); 
          }
        }
      }
    }.setup(snmpManager, textViewRes)).start();
  }
}

