// OID.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class OID extends Variable {

  private int oid[];

  public OID(String str, int length) {
    this.length = length;
  }

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputStream os) throws IOException { }
}

