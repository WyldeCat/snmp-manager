// Counter32.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class Counter32 extends Variable {

  public long value;

  public Counter32() { }

  public Counter32(int value, int length) {
    this.value = value;
    this.length = length;
  }

  public String toString() {
    return Long.toString(value);
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    long prev = bis.getPosition();
    
    value = BER.decodeUnsignedInteger(bis, new BER.MutableByte());
    length = (int)(bis.getPosition() - prev - 2);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeUnsignedInteger(os, BER.GAUGE32, value);
  }
}
