// OctetString.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

import android.util.Log;

public class OctetString extends Variable {

  private String str;

  public OctetString() { }

  public OctetString(String str) {
    this.str = str;
    this.length = str.length();
  }

  public String toString() {
    return str;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    BER.MutableByte b = new BER.MutableByte();
    str = new String(BER.decodeString(bis, b),
      Charset.forName("US-ASCII"));
    length = str.length();
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeString(os, BER.OCTETSTRING,
      str.getBytes(Charset.forName("US-ASCII")));
  }
}

