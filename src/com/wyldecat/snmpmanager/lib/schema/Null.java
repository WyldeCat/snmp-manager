// Null.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class Null extends Variable {

  public Null() {
    length = 0;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    BER.MutableByte b = new BER.MutableByte();
    BER.decodeNull(bis, b);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeHeader(os, BER.NULL, length);
  }
}

