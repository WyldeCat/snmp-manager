// SnmpManager.java

package com.wyldecat.snmpmanager.lib;

import java.net.*;
import java.util.Arrays;
import android.util.Log;

import com.wyldecat.snmpmanager.lib.SnmpSchema.*;

public class SnmpManager {

  private final DatagramSocket sock;

  private final byte buff_recv[] = new byte[1024];
  private final byte buff_send[] = new byte[1024];

  private final DatagramPacket pkt_recv =
    new DatagramPacket(buff_recv, buff_recv.length);
  private final DatagramPacket pkt_send =
    new DatagramPacket(buff_send, buff_send.length);

  private final Message m = new Message();

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

  public void Get(String OID) throws Exception {
    OID = checkOID(OID);

    m.getPDU().setType(PDU.Type.GET_REQUEST);
    m.getPDU().setRequestID((byte)4, 0x1234);
    m.getPDU().setErrorStatus((byte)1, 0x00);
    m.getPDU().setErrorIdx((byte)1, 0x00);

    m.getPDU().getVarbindList().setLength(1);

    Varbind vb = new Varbind();
    vb.variable = new Data();
    vb.variable.setOID((byte)OID.split("\\.").length, OID);
    vb.value = new Data();
    vb.value.setNull();

    m.getPDU().getVarbindList().setVarbindAt(0, vb);

    m.toBytes(buff_send, 0);

    try {
      sock.send(pkt_send);
      sock.receive(pkt_recv);
    } catch (Exception e) { }
    
    Log.d("[SnmpManager]", Arrays.toString(buff_recv)); 

    m.fromBytes(buff_recv, 0);

    vb = m.getPDU().getVarbindList().getVarbindAt(0);

    Log.d("[SnmpManager]", Arrays.toString(vb.variable.value)); 
    Log.d("[SnmpManager]", Arrays.toString(vb.value.value)); 
  }

  public void Set() {}
  public void Walk() {}
}
