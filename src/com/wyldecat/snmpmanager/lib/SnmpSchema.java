// SnmpSchema.java

package com.wyldecat.snmpmanager.lib;

import java.util.*;

interface ByteCompatible {
  /* Returns length of data including header */
  public byte getLength();
  public int fromBytes(byte bytes[], int offset);
  public int toBytes(byte bytes[], int offset);
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

      public final byte type_;
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

    public byte getLength() {
      return (byte)(2 + length);
    }

    public int fromBytes(byte bytes[], int offset) {
      type = Type.convert(bytes[offset]);
      length = bytes[offset + 1];
      value = Arrays.copyOfRange(bytes, offset + 2, length);

      return offset + 3;
    }

    public int toBytes(byte bytes[], int offset) {
      bytes[offset++] = type.type_;
      bytes[offset++] = length;
      System.arraycopy(value, 0, bytes, offset, length);

      return offset + length;
    }
  }

  static public class Varbind implements ByteCompatible {
    public Data variable;
    public Data value;

    public byte getLength() {
      return (byte)(2 + variable.getLength() + value.getLength());
    }

    public int fromBytes(byte bytes[], int offset) {
      offset = variable.fromBytes(bytes, offset);
      offset = value.fromBytes(bytes, offset);

      return offset;
    }

    public int toBytes(byte bytes[], int offset) {
      bytes[offset++] = 0x30;
      bytes[offset++] = (byte)(variable.getLength() + value.getLength());
      offset = variable.toBytes(bytes, offset);
      offset = value.toBytes(bytes, offset);

      return offset;
    }
  }

  static public class VarbindList implements ByteCompatible {
    public Varbind varbinds[];

    public byte getLength() {
      int length = 0;

      for (ByteCompatible bc: varbinds) {
        length += bc.getLength();
      }

      return (byte)(2 + length);
    }

    public int fromBytes(byte bytes[], int offset) {
      int length = bytes[offset + 1];
      varbinds = new Varbind[length];

      offset += 2;
      for (int i = 0; i < length; i++) {
        offset = varbinds[i].fromBytes(bytes, offset);
      }

      return offset;
    }

    public int toBytes(byte bytes[], int offset) {
      int tmp = offset;
      bytes[offset++] = 0x30;
      bytes[offset++] = (byte)(getLength() - 2);

      for (int i = 0; i < varbinds.length; i++) {
        offset = varbinds[i].toBytes(bytes, offset);
      }

      return offset;
    }
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

      public final byte pdu_type_;
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

      public final int error_status_;
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

    public byte getLength() {
      return (byte)(2 + request_id.getLength() + error_status.getLength()
        + error_idx.getLength() + varbind_list.getLength());
    }

    public int fromBytes(byte bytes[], int offset) {
      pdu_type = PDUType.convert(bytes[offset]);
      length = bytes[offset + 1];
      offset += 2;

      offset = request_id.fromBytes(bytes, offset);
      offset = error_status.fromBytes(bytes, offset);
      offset = error_idx.fromBytes(bytes, offset);
      offset = varbind_list.fromBytes(bytes, offset);

      return offset;
    }

    public int toBytes(byte bytes[], int offset) {
      bytes[offset++] = pdu_type.pdu_type_;
      bytes[offset++] = length;
      
      offset = request_id.toBytes(bytes, offset);
      offset = error_status.toBytes(bytes, offset);
      offset = error_idx.toBytes(bytes, offset);

      offset = varbind_list.toBytes(bytes, offset);

      return offset ;
    }
  }
}
