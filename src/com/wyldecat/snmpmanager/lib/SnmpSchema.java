// SnmpSchema.java

package com.wyldecat.snmpmanager.lib;

interface ByteCompatible {
  public int getLength();
  public void fromByte();
  public void toByte(byte bytes[], int offset);
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
    }

    public Type type;
    public byte length;
    public byte value[];

    public Data(Type type, byte length) {
      this.type = type;
      this.length = length;
      value = new byte[length];
    }

    public int getLength() {}
    public void fromByte() {}
    public void toByte(byte bytes[], int offset) {}
  }

  static public class VarbindList implements ByteCompatible {
    public Varbind varbinds[];

    public int getLength() {}
    public void fromByte() {}
    public void toByte(byte bytes[], int offset) {}
  }

  static public class Varbind implements ByteCompatible {
    public Data Variable;
    public Data Value;

    public int getLength() {}
    public void fromByte() {}
    public void toByte(byte bytes[], int offset) {}
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
    }

    public PDUType pdu_type;
    public byte length;

    public Data request_id;
    public Data error_status;
    public Data error_idx;

    public VarbindList varbind_list;

    public int getLength() {}
    public void fromByte() {}
    public void toByte(byte bytes[], int offset) {}
  }
}
