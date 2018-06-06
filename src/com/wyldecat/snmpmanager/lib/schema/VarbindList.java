// VarbindList.java

package com.wyldecat.snmpmanager.lib.schema;

import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERSerializable;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Varbind;

public class VarbindList implements BERSerializable {

  private int length;
  private ArrayList<Varbind> varbinds;

  public VarbindList() {
    varbinds = new ArrayList(1);
    varbinds.add(new Varbind());
    length = 2;
  }

  public VarbindList(int initialCapacity) {
    varbinds = new ArrayList(initialCapacity);
    varbinds.add(new Varbind());
    length = 2;
  }

  public void addVarbind(Varbind vb) {
    varbinds.add(vb);
    length += vb.getBERLength();
  }

  public void setVarbindAt(int idx, Varbind vb) {
    if (varbinds.get(idx) != null) {
      length -= varbinds.get(idx).getBERLength();
    }

    varbinds.set(idx, vb);
    length += vb.getBERLength();
  }

  public Varbind getVarbindAt(int idx) {
    return varbinds.get(idx);
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

    for (int i = 0; i < varbinds.size(); i++) {
      if (varbinds.get(i) == null) throw new IOException();
      varbinds.get(i).encodeBER(os);
    }
  }
}

