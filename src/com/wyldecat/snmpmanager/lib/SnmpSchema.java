// SnmpSchema.java

package com.wyldecat.snmpmanager.lib;

import java.util.Arrays;
import java.nio.charset.Charset;

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

    public Data() {}

    public Data(Type type, byte len) {
      this.type = type;
      this.length = len;
      this.value = new byte[len];
    }

    public Data(Type type, byte len, int val) {
      this.type = type;
      this.length = len;
      this.value = new byte[len];

      for (int i = len - 1; i >= 0; i--) {
        this.value[i] = (byte)(val & 0xff);
        val >>= 0xff;
      }
    }

    public byte getLength() {
      return (byte)(2 + length);
    }

    public int fromBytes(byte bytes[], int offset) {
      type = Type.convert(bytes[offset]);
      length = bytes[offset + 1];
      value = Arrays.copyOfRange(bytes, offset + 2, offset + 2 + length);

      return offset + 2 + length;
    }

    public int toBytes(byte bytes[], int offset) {
      bytes[offset++] = type.type_;
      bytes[offset++] = length;

      if (value != null) {
        System.arraycopy(value, 0, bytes, offset, length);
      }

      return offset + length;
    }

    public void setOID(byte len, String oid) {
      type = Type.OBJECT_IDENTIFIER;
      length = len;
      value = new byte[len];

      String tmp[] = oid.split("\\.");
      for (int i = 0; i < len; i++) {
        value[i] = (byte)Integer.parseInt(tmp[i]);
      }
    }

    public void setNull() {
      type = Type.NULL;
      length = 0;
      value = null;
    }

    public String toString() {
      switch (type) {
      case INTEGER: {
        int val = 0;
        for (int i = length - 1; i >= 0; i--) {
          val += value[i];
          val <<= 1;
        }
        return Integer.toString(val); 
      }
      case OCTET_STRING: {
        return new String(value, Charset.forName("US-ASCII"));
      }
      case OBJECT_IDENTIFIER: {
        String ret = "";
        for (int i = 0; i < length; i++) {
          ret += Integer.toString(value[i]) +
            ((i != length - 1) ? "." : "");
        }
        return ret; 
      }
      case NULL: {
        return "";
      }
      case SEQUENCE: {
        return "";
      }
      case IPADDRESS: {
        return "";
      }
      case COUNTER: {
        return "";
      }
      case GAUGE: {
        return "";
      }
      case TIMETICKS: {
        return "";
      }
      case OPAQUE: {
        return "";
      }
      }

      return "";
    }
  }

  static public class Varbind implements ByteCompatible {
    public Data variable;
    public Data value;

    public byte getLength() {
      return (byte)(2 + variable.getLength() + value.getLength());
    }

    public int fromBytes(byte bytes[], int offset) {
      offset += 2;

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

    public Varbind() {
      variable = new Data();
      value = new Data();
    }

    public String toString() {
      return variable.toString() + " = " + value.toString();
    }
  }

  static public class VarbindList implements ByteCompatible {
    private Varbind varbinds[];

    public byte getLength() {
      int length = 0;

      for (ByteCompatible bc: varbinds) {
        length += bc.getLength();
      }

      return (byte)(2 + length);
    }

    public int fromBytes(byte bytes[], int offset) {
      int length = bytes[offset + 1];
      int cnt = 0, idx = offset + 2;

      while (length > 0) {
        cnt++;
        length -= 2 + bytes[idx + 1];
        idx += 2 + bytes[idx + 1];
      }

      varbinds = new Varbind[cnt];

      offset += 2;
      for (int i = 0; i < cnt; i++) {
        varbinds[i] = new Varbind();
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

    public void setLength(int len) {
      varbinds = new Varbind[len];
    }

    public void setVarbindAt(int idx, Varbind vb) {
      if (varbinds.length <= idx) return;
      varbinds[idx] = vb;
    }

    public final Varbind getVarbindAt(int idx) {
      return varbinds[idx];
    }
  }

  static public class PDU implements ByteCompatible {
    public enum Type {
      GET_REQUEST       ((byte)0xA0),
      GET_NEXT_REQUEST  ((byte)0xA1),
      RESPONSE          ((byte)0xA2),
      SET_REQUEST       ((byte)0xA3),
      GET_BULK_REQUEST  ((byte)0xA5),
      INFORM_REQUEST    ((byte)0xA6),
      TRAP              ((byte)0xA7),
      REPORT            ((byte)0xA8);

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

    private Type type;
    private byte length;

    private Data request_id;
    private Data error_status;
    private Data error_idx;

    private VarbindList varbind_list;

    public byte getLength() {
      return (byte)(2 + request_id.getLength() + error_status.getLength()
        + error_idx.getLength() + varbind_list.getLength());
    }

    public int fromBytes(byte bytes[], int offset) {
      type = Type.convert(bytes[offset]);
      length = bytes[offset + 1];
      offset += 2;

      offset = request_id.fromBytes(bytes, offset);
      offset = error_status.fromBytes(bytes, offset);
      offset = error_idx.fromBytes(bytes, offset);
      offset = varbind_list.fromBytes(bytes, offset);

      return offset;
    }

    public int toBytes(byte bytes[], int offset) {
      bytes[offset++] = type.type_;
      if (length == 0) length = (byte)(getLength() - 2);
      bytes[offset++] = length;
      
      offset = request_id.toBytes(bytes, offset);
      offset = error_status.toBytes(bytes, offset);
      offset = error_idx.toBytes(bytes, offset);
      offset = varbind_list.toBytes(bytes, offset);

      return offset;
    }

    public PDU() {
      length = 0;

      request_id = new Data();
      error_status = new Data();
      error_idx = new Data();
      varbind_list = new VarbindList();
    }

    public void setType(Type type) {
      this.type = type;
    }

    public void setRequestID(byte len, int request_id_) {
      request_id = new Data(Data.Type.INTEGER, len, request_id_);
    }

    public void setErrorStatus(byte len, int error_status_) {
      error_status = new Data(Data.Type.INTEGER, len, error_status_);
    }

    public void setErrorIdx(byte len, int error_idx_) {
      error_idx = new Data(Data.Type.INTEGER, len, error_idx_);
    }

    public VarbindList getVarbindList() {
      return varbind_list;
    }
  }

  static public class Message implements ByteCompatible {
    private byte length;
    private Data version;
    private Data community_string;
    private PDU pdu;

    public byte getLength() {
      if (length == 0) {
        length = (byte)(version.getLength() + community_string.getLength()
          + pdu.getLength());
      }

      return (byte)(length + 2);
    }

    public int fromBytes(byte bytes[], int offset) {
      length = bytes[offset + 1];
      offset += 2;

      offset = version.fromBytes(bytes, offset);
      offset = community_string.fromBytes(bytes, offset);
      offset = pdu.fromBytes(bytes, offset);
      
      return offset;
    }

    public int toBytes(byte bytes[], int offset) {
      if (length == 0) {
        getLength();
      }

      bytes[offset++] = 0x30;
      bytes[offset++] = length;
      offset = version.toBytes(bytes, offset);
      offset = community_string.toBytes(bytes, offset);
      offset = pdu.toBytes(bytes, offset);

      return offset;
    }

    public Message() {
      length = 0;

      version = new Data(Data.Type.INTEGER, (byte)0x01);
      version.value[0] = 0x01;

      community_string = new Data(Data.Type.OCTET_STRING, (byte)0x06);
      community_string.value[0] = 0x70;
      community_string.value[1] = 0x75;
      community_string.value[2] = 0x62;
      community_string.value[3] = 0x6c;
      community_string.value[4] = 0x69;
      community_string.value[5] = 0x63;

      pdu = new PDU();
    }

    public PDU getPDU() {
      return pdu;
    }
  }
}
