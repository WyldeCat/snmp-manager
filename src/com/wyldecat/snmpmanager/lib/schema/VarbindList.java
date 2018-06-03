// VarbindList.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERSerializable;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Varbind;

public class VarbindList implements BERSerializable {

  private int length;
  private Varbind varbinds[];

  public VarbindList(int capacity) {
    varbinds = new Varbind[];
    length = 0;
  }

  public void setVarbindAt(int idx, Varbind vb) {
    varbinds[idx] = vb;
    updateLength(); 
  }

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  }

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputStream os) throws IOException { }

  private void updateLength() {
    length = 0;
    for (Varbind vb: varbinds) {
      if (vb == null) continue;
      length += vb.getBERLength();
    }
  }
}

