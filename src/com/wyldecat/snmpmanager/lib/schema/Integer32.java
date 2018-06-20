// Integer32.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class Integer32 extends Variable {

  public int value;

  public Integer32() { }

  public Integer32(int value) throws Exception {
    byte tmp[] = new byte[16];
    BERInputStream bis = new BERInputStream(ByteBuffer.wrap(tmp));
    BEROutputStream bos = new BEROutputStream(ByteBuffer.wrap(tmp));

    BER.encodeInteger(bos, BER.INTEGER32, value);
    this.value = value;
    this.length = BER.decodeHeader(bis, new BER.MutableByte());
  }

  public Integer32(int value, int length) {
    this.value = value;
    this.length = length;
  }

  public String toString() {
    return Integer.toString(value);
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    long prev = bis.getPosition();

    value = BER.decodeInteger(bis, new BER.MutableByte());
    length = (int)(bis.getPosition() - prev - 2);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeInteger(os, BER.INTEGER32, value);
  }
}

