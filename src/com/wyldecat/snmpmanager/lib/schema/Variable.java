// Variable.java

package com.wyldecat.snmpmanager.lib.schema;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERSerializable;

public abstract class Variable implements BERSerializable {

  protected int length;

  abstract public String toString();

  public int getBERLength() {
    return length + 1 + BER.getBERLengthOfLength(length);
  }

  public int getBERPayloadLength() {
    return length;
  }
}

