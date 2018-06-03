// PDU.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;

public class PDU implements BERSerializable {

  public int getBERLength() {
    return 0;
  }

  public int getBERPayloadLength() {
    return 0;
  } 

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputStream os) throws IOException { }
}

