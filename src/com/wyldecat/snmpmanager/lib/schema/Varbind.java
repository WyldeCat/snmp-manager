// Varbind.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERSerializable;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;
import com.wyldecat.snmpmanager.lib.schema.OID;

public class Varbind implements BERSerializable {

  private int length;
  private OID variable;
  private Variable value;

  public Varbind(OID variable, Variable value) {
    this.variable = variable;
    this.value = value;
    this.length = variable.getBERLength() + value.getBERLength();
  }

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  }

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputStream os) throws IOException { }
}

