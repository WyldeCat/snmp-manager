// Message.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;

public class Message implements BERSerializable {

  private int length;

  private Integer32 snmpVersion;
  private OctetString communityString;
  private PDU pdu;

  public Message() {
    snmpVersion = new Integer32();
    communityString = new OctetString();
    pdu = new PDU();
  }

  public Message setSnmpVersion(int version, int length) {
    snmpVersion = new Integer32(version, length);
    return this;
  }

  public Message setCommunityString(String str) {
    communityString = new OctetString(str);
    return this;
  }

  public Message setPDU(PDU pdu) {
    this.pdu = pdu;
    return this;
  }

  public PDU getPDU() {
    return pdu;
  }

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    length = BER.decodeHeader(bis, new BER.MutableByte()); 
    snmpVersion.decodeBER(bis);
    communityString.decodeBER(bis);
    pdu.decodeBER(bis);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeHeader(os, BER.SEQUENCE, length);
    snmpVersion.encodeBER(os);
    communityString.encodeBER(os);
    pdu.encodeBER(os);
  }
}

