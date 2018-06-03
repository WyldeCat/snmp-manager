// OctetString.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class OctetString extends Variable {

  private String str;

  public OctetString(String str) {
    this.str = str;
    this.length = str.length;
  }

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputString os) throws IOException { }
}

