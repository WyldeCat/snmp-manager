// PDU.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;
import com.wyldecat.snmpmanager.lib.schema.Integer32;
import com.wyldecat.snmpmanager.lib.schema.Varbind;
import com.wyldecat.snmpmanager.lib.schema.VarbindList;

public class PDU implements BERSerializable {

  private int length;

  private BER.MutableByte type;
  private Integer32 requestID;
  private Integer32 errorStatus;
  private Integer32 errorIndex;
  private VarbindList varbinds;

  public PDU() {
    type = new BER.MutableByte();
    requestID = new Integer32();
    errorStatus = new Integer32();
    errorIndex = new Integer32();
    varbinds = new VarbindList(1);
  }

  public PDU setType(byte type) {
    this.type = new BER.MutableByte(type);
    return this;
  }

  public PDU setRequestID(int requestID, int rIDLength) {
    this.requestID = new Integer32(requestID, rIDLength);
    updateLength();
    return this;
  }

  public PDU setErrorStatus(int errorStatus, int esLength) {
    this.errorStatus = new Integer32(errorStatus, esLength);
    return this;
  }

  public PDU setErrorIndex(int errorIndex, int eiLength) {
    this.errorIndex = new Integer32(errorIndex, eiLength);
    return this;
  }

  public BER.MutableByte getType() {
    return type;
  }

  public Integer32 getRequestID() {
    return requestID;
  }

  public Integer32 getErrorStatus() {
    return errorStatus;
  }

  public Integer32 getErrorIndex() {
    return errorIndex;
  }

  public VarbindList getVarbindList() {
    return varbinds;
  }

  public void updateLength() {
    length = requestID.getBERLength();
    length += errorStatus.getBERLength();
    length += errorIndex.getBERLength();
    length += varbinds.getBERLength();
  }

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
    BER.encodeHeader(os, type.getValue(), length);
    requestID.encodeBER(os);
    errorStatus.encodeBER(os);
    errorIndex.encodeBER(os);
    varbinds.encodeBER(os);
  }
}

