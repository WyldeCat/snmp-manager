// OID.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class OID extends Variable {

  private int oid[];

  public OID() { }

  public OID(String str) {
    String splited[] = str.split("\\.");
    byte buf[] = new byte[512];

    oid = new int[splited.length];
    for (int i = 0; i < splited.length; i++) {
      oid[i] = Integer.parseInt(splited[i]);
    }

    BERInputStream bis = new BERInputStream(ByteBuffer.wrap(buf));
    BEROutputStream bos = new BEROutputStream(ByteBuffer.wrap(buf));

    try {
      BER.encodeOID(bos, BER.OID, oid);
      this.length = BER.decodeHeader(bis, new BER.MutableByte());
    } catch(Exception ignore) { }
  }

  public String toString() {
    String ret = Integer.toString(oid[0]);

    for (int i = 1; i < oid.length; i++) {
      ret += "." + Integer.toString(oid[i]);    
    }

    return ret;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    oid = BER.decodeOID(bis, new BER.MutableByte());
    length = oid.length;
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeOID(os, BER.OID, oid); 
  }
}

