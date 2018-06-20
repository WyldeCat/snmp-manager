// SnmpManager.java

package com.wyldecat.snmpmanager.lib;

import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.ByteBuffer;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;

import com.wyldecat.snmpmanager.lib.schema.*;

public class SnmpManager {

  private final DatagramSocket sock;

  private final byte buff_recv[] = new byte[1024];
  private final byte buff_send[] = new byte[1024];

  private final DatagramPacket pkt_recv =
    new DatagramPacket(buff_recv, buff_recv.length);
  private final DatagramPacket pkt_send =
    new DatagramPacket(buff_send, buff_send.length);

  private Message m_recv;
  private Message m_send;

  private final AtomicBoolean isWorking
    = new AtomicBoolean(false);

  public SnmpManager(String addr, int port) {

    DatagramSocket _sock = null;
    InetAddress deviceAddr = null;

    try {
      _sock = new DatagramSocket(11501);
      _sock.setSoTimeout(10000);
      deviceAddr = InetAddress.getByName(addr);
    } catch (Exception ignore) { }

    sock = _sock;

    pkt_send.setAddress(deviceAddr);
    pkt_send.setPort(port);

    m_recv = new Message();
    m_send = new Message();

    m_send.setSnmpVersion(1, 1)
      .setCommunityString(new String("public"))
      .setPDU(
        new PDU().setRequestID(0x1234, 2)
          .setErrorStatus(0, 1)
          .setErrorIndex(0, 1)
    );
  }

  private String checkOID(String OID) throws Exception {
    if (OID.charAt(0) == '1' && OID.charAt(1) == '.')  {
      int nextDot = 1;
      int calculated = 0;

      while (OID.charAt(++nextDot) != '.');

      calculated =
        40 + Integer.parseInt(OID.substring(2, nextDot));

      return Integer.toString(calculated) + OID.substring(nextDot);
    }

    return OID;
  }

  private String request(String oid, Variable val, byte type) throws Exception {
    BEROutputStream bos = new BEROutputStream(ByteBuffer.wrap(buff_send));
    BERInputStream bis = new BERInputStream(ByteBuffer.wrap(buff_recv));

    m_send.getPDU().setType(type);
    m_send.getPDU().getVarbindList().setVarbindAt(0, new Varbind(
        new OID(oid), val
      )
    );
    m_send.updateLength();
    m_send.encodeBER(bos);

    pkt_send.setLength(m_send.getBERLength());

    while (true) {
      sock.send(pkt_send);
      try {
        sock.receive(pkt_recv);
        break;
      } catch (SocketTimeoutException ste) { }
    }

    m_recv.decodeBER(bis);

    return m_recv.getPDU().getVarbindList().getVarbindAt(0).toString();
  }

  public boolean isWorking() {
    return isWorking.get();
  }

  public void setIsWorking() {
    isWorking.set(true);
  }

  public String Get(String oid) throws Exception {
    return request(oid, new Null(), (byte)0xa0);
  }

  public void Walk(Handler handler) throws Exception {
    String oid = "1.3.6.1.2.1";
    String ret;
    Variable val;
    android.os.Message msg;

    m_send.getPDU().setRequestID((int)(Math.random() * 0xFFFFFFFF), 4);

    try {
      while (true) {
        ret = request(oid, new Null(), (byte)0xa1);
        oid = m_recv.getPDU().getVarbindList()
          .getVarbindAt(0).getVariable().toString();

        msg = handler.obtainMessage();
        msg.obj = ret;
        handler.sendMessage(msg);

        val = m_recv.getPDU().getVarbindList().getVarbindAt(0).getValue();

        if (val instanceof EndOfMIBView) {
          break;
        }
      }

      isWorking.set(false);
    } catch (Exception e) {
      isWorking.set(false);
      throw e;
    }
  }

  public void Set(Handler handler, String oid, String val) throws Exception {
    request(oid, new Null(), (byte)0xa0); 
    Variable v = m_recv.getPDU().getVarbindList()
      .getVarbindAt(0).getVariable();

    if (v instanceof Integer32) {
      v = new Integer32(Integer.parseInt(val));
    }
    else if (v instanceof OctetString) {
      v = new OctetString(val);
    }
    else if (v instanceof OID) {
      v = new OID(val);
    }
    else {
      throw new Exception("Unsupported type");
    }

    request(oid, v, (byte)0xa2);
  }
}

