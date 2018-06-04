// VarbindList.java

package com.wyldecat.snmpmanager.lib.schema;

import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERSerializable;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Varbind;

public class VarbindList implements BERSerializable {

  private int length;
  private ArrayList<Varbind> varbinds;

  public VarbindList() { }

  public VarbindList(int initialCapacity) {
    varbinds = new ArrayList(initialCapacity);
    length = 0;
  }

  public void addVarbind(Varbind vb) {
    varbinds.add(vb);
    length += vb.getBERLength();
  }

  public void setVarbindAt(int idx, Varbind vb) {
    if ((Varbind oldVb = varbinds.get(idx)) != null) {
      length -= oldVb.getBERLength();
    }

    varbinds.set(idx, vb);
    length += vb.getBERLength();
  }

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    int _length;

    length = _length = BER.decodeHeader(bis, new BER.MutableByte());
    varbinds.clear();

    while (_length > 0) {
      Varbind vb = new Varbind();
      vb.decodeBER(bis);
      _length -= vb.getBERLength();
      varbinds.add(vb);
    }
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeHeader(os,
      BER.SEQUENCE, length);

    for (int i = 0; i < varbinds.length; i++) {
      if (varbinds[i] == null) throws new IOException();
      varbinds[i].encodeBER(os);
    }
  }
}

