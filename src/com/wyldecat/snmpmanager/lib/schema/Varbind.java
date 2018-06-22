// Varbind.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERSerializable;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.*;

public class Varbind implements BERSerializable {

  private int length;
  private OID variable;
  private Variable value;

  public Varbind() {
    variable = new OID();
    length = 0;
  }

  public Varbind(OID variable, Variable value) {
    this.variable = variable;
    this.value = value;
    this.length = variable.getBERLength() + value.getBERLength();
  }

  public OID getVariable() {
    return variable;
  }

  public Variable getValue() {
    return value;
  }

  public String toString() {
    String type = "UNKNOWN";
    if (value instanceof Counter32) {
      type = "COUNTER";
    } else if (value instanceof EndOfMIBView) {
      type = "ENDOFMIBVIEW";
    } else if (value instanceof Gauge32) {
      type = "GAUGE";
    } else if (value instanceof Integer32) {
      type = "INTEGER";
    } else if (value instanceof OctetString) {
      type = "OCTETSTRING";
    } else if (value instanceof OID) {
      type = "OID";
    }

    return variable.toString() + " = " + type + ": " + value.toString();
  }

  public int getBERLength() {
    return length + 1 + BER.getBERLengthOfLength(length);
  }

  public int getBERPayloadLength() {
    return length;
  }

  public void decodeBER(BERInputStream bis) throws IOException {
    BER.MutableByte valueType = new BER.MutableByte();
    length = BER.decodeHeader(bis, new BER.MutableByte());

    variable.decodeBER(bis);

    bis.mark(length);
    BER.decodeHeader(bis, valueType);
    bis.reset();

    switch (valueType.getValue() & 0xFF) {
    case BER.INTEGER32: {
      value = new Integer32();
      break;
    }
    case BER.OCTETSTRING: {
      value = new OctetString();
      break;
    }
    case BER.NULL: {
      value = new Null();
      break;
    }
    case BER.OID: {
      value = new OID();
      break;
    }
    case BER.TIMETICKS:
    case BER.GAUGE32: {
      value = new Gauge32();
      break;
    }
    case BER.COUNTER32: {
      value = new Counter32();
      break;
    }
    case BER.ENDOFMIBVIEW: {
      value = new EndOfMIBView();
      break;
    }
    default: {
      throw new IOException();
    }
    }

    value.decodeBER(bis);
  }

  public void encodeBER(OutputStream os) throws IOException {
    BER.encodeHeader(os, BER.SEQUENCE,
      variable.getBERLength() + value.getBERLength());

    variable.encodeBER(os);
    value.encodeBER(os);
  }
}

