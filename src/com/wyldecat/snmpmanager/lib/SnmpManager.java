// SnmpManager.java

package com.wyldecat.snmpmanager.lib;

import java.net.*;
import java.util.Arrays;
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

  public SnmpManager(String addr, int port) {
    DatagramSocket _sock = null;
    InetAddress deviceAddr = null;

    try {
      _sock = new DatagramSocket(11501);
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

  private String get(String oid, boolean isNextRequest) throws Exception {
    BEROutputStream bos = new BEROutputStream(ByteBuffer.wrap(buff_send));
    BERInputStream bis = new BERInputStream(ByteBuffer.wrap(buff_recv));

    m_send.getPDU().setType((isNextRequest ? (byte)0xa1 : (byte)0xa0));
    m_send.getPDU().getVarbindList().setVarbindAt(0, new Varbind(
        new OID(oid), new Null()
      )
    );
    m_send.updateLength();
    m_send.encodeBER(bos);

    pkt_send.setLength(m_send.getBERLength());

    sock.send(pkt_send);
    sock.receive(pkt_recv);

    m_recv.decodeBER(bis);

    return m_recv.getPDU().getVarbindList().getVarbindAt(0).toString();
  }

  public String Get(String oid) throws Exception {
    return get(oid, false);
  }

  public void Walk(Handler handler) throws Exception {
    String oid = "1.2.1";
    String ret;
    android.os.Message msg;
    int num_step = 16;

    while (num_step-- > 0) {
      ret = get(oid, true);
      oid = m_recv.getPDU().getVarbindList().getVarbindAt(0).getVariable().toString();
      Log.d("snmp", oid);

      msg = handler.obtainMessage();
      msg.obj = ret;
      handler.sendMessage(msg);
    }
  }

  public void Set() { }
}
