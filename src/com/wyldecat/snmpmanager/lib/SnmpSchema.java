// SnmpSchema.java

package com.wyldecat.snmpmanager.lib;

import java.util.*;

interface ByteCompatible {
  /* Returns length of data including header */
  public int getLength();
  public void fromBytes(byte bytes[], int offset);
  public void toBytes(byte bytes[], int offset);
}

public class SnmpSchema {
  /* Data using Basic Encoding Rules */
  static public class Data implements ByteCompatible { 
    public enum Type {
      INTEGER           ((byte)0x02),
      OCTET_STRING      ((byte)0x04),
      OBJECT_IDENTIFIER ((byte)0x06),
      NULL              ((byte)0x05),
      SEQUENCE          ((byte)0x30),
      IPADDRESS         ((byte)0x40),
      COUNTER           ((byte)0x41),
      GAUGE             ((byte)0x42),
      TIMETICKS         ((byte)0x43),
      OPAQUE            ((byte)0x44);

      private final byte type_;
      private Type(byte type) {
        this.type_ = type;
      }

      static public Type convert(byte b) {
        for (Type t: Type.values()) {
          if (b == t.type_) {
            return t;
          }
        }
        return null;
      }
    }

    public Type type;
    public byte length;
    public byte value[];

    public Data(Type type, byte length) {
      this.type = type;
      this.length = length;
      value = new byte[length];
    }

    public int getLength() {
      return 2 + length;
    }

    public void fromBytes(byte bytes[], int offset) {
      type = Type.convert(bytes[offset]);
      length = bytes[offset + 1];
      value = Arrays.copyOfRange(bytes, offset + 2, length);
    }

    public void toBytes(byte bytes[], int offset) {}
  }

  static public class VarbindList implements ByteCompatible {
    public Varbind varbinds[];

    public int getLength() {
      int length = 0;

      for (ByteCompatible bc: varbinds) {
        length += bc.getLength();
      }

      return 2 + length;
    }

    public void fromBytes(byte bytes[], int offset) {
      int length = bytes[offset + 1];
      varbinds = new Varbind[length];

      offset += 2;
      for (int i = 0; i < length; i++) {
        varbinds[i].fromBytes(bytes, offset);
        offset += varbinds[i].getLength();
      }
    }

    public void toBytes(byte bytes[], int offset) {}
  }

  static public class Varbind implements ByteCompatible {
    public Data variable;
    public Data value;

    public int getLength() {
      return 2 + variable.getLength() + value.getLength();
    }

    public void fromBytes(byte bytes[], int offset) {
      variable.fromBytes(bytes, offset);
      value.fromBytes(bytes, offset + variable.getLength());
    }

    public void toBytes(byte bytes[], int offset) {}
  }

  static private class Packet implements ByteCompatible {
    public enum PDUType {
      GET_REQUEST       ((byte)0xA0),
      GET_NEXT_REQUEST  ((byte)0xA1),
      RESPONSE          ((byte)0xA2),
      SET_REQUEST       ((byte)0xA3),
      GET_BULK_REQUEST  ((byte)0xA5),
      INFORM_REQUEST    ((byte)0xA6),
      TRAP              ((byte)0xA7),
      REPORT            ((byte)0xA8);

      private final byte pdu_type_;
      private PDUType(byte pdu_type) {
        this.pdu_type_ = pdu_type;
      }

      static public PDUType convert(byte b) {
        for (PDUType t: PDUType.values()) {
          if (b == t.pdu_type_) {
            return t;
          }
        }
        return null;
      }
    }

    public enum ErrorStatus {
      NO_ERROR          (0x00),
      TOO_BIG           (0x01),
      NO_SUCH_NAME      (0x02),
      BAD_VALUE         (0x03),
      READ_ONLY         (0x04),
      GEN_ERR           (0x05);

      private final int error_status_;
      private ErrorStatus(int error_status) {
        this.error_status_ = error_status;
      }

      static public ErrorStatus convert(byte b) {
        for (ErrorStatus t: ErrorStatus.values()) {
          if (b == t.error_status_) {
            return t;
          }
        }
        return null;
      }
    }

    public PDUType pdu_type;
    public byte length;

    public Data request_id;
    public Data error_status;
    public Data error_idx;

    public VarbindList varbind_list;

    public int getLength() {
      return 2 + request_id.getLength() + error_status.getLength()
        + error_idx.getLength() + varbind_list.getLength();
    }

    public void fromBytes(byte bytes[], int offset) {
      pdu_type = PDUType.convert(bytes[offset]);
      length = bytes[offset + 1];
      offset += 2;

      request_id.fromBytes(bytes, offset);
      offset += request_id.getLength();

      error_status.fromBytes(bytes, offset);
      offset += error_status.getLength();

      error_idx.fromBytes(bytes, offset);
    }

    public void toBytes(byte bytes[], int offset) {}
  }
}
