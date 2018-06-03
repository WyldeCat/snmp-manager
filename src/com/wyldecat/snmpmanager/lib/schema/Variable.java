// Variable.java

package com.wyldecat.snmpmanager.lib.schema;

import org.snmp4j.asn1.BERSerializable;

public abstract class Variable implements BERSerializable {

  protected int length; 

  public int getBERLength() {
    return length + 2;
  }

  public int getBERPayloadLength() {
    return length;
  }
}

