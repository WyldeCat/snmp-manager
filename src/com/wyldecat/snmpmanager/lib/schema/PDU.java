// PDU.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;

public class PDU implements BERSerializable {

  private int length;

  private BER.MutableByte type;
  private Integer32 requestID;
  private Integer32 errorStatus;
  private Integer32 errorIndex;
  private VarbindList varbinds;

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  } 

  public void decodeBER(BERInputStream bis) throws IOException {
    length = BER.decodeHeader(bis, type);
    requestID.decodeBER(bis);
    errorStatus.decodeBER(bis);
    errorIndex.decodeBER(bis);
    varbinds.decodeBER(bis);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeHeader(os, type, length);
    requestId.encodeBER(os);
    errorStatus.encodeBER(os);
    errorIndex.encodeBER(os);
    varbinds.encodeBER(os);
  }
}

