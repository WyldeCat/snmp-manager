// SnmpManager.java

package com.wyldecat.snmpmanager.lib;

import java.net.*;
import java.util.Arrays;
import android.util.Log;

import com.wyldecat.snmpmanager.lib.SnmpSchema.*;

public class SnmpManager {

  private final DatagramSocket sock;
  private final InetAddress deviceAddr;
  private final int devicePort;

  public SnmpManager(String addr, int port) {
    DatagramSocket _sock = null;
    InetAddress _deviceAddr = null;

    try {
      _sock = new DatagramSocket(11501);
      _deviceAddr = InetAddress.getByName(addr);
    } catch (Exception ignore) { }

    sock = _sock;
    deviceAddr = _deviceAddr;
    devicePort = port;
  }
  
  public void Get() {
    byte buff[]; 
    byte buff_recv[] = new byte[1024];
    DatagramPacket dp;
    DatagramPacket dp_recv = new DatagramPacket(buff_recv, buff_recv.length);

    Message m = new Message();

    m.getPDU().setType(PDU.Type.GET_REQUEST);
    m.getPDU().setRequestID((byte)4, 0x1234);
    m.getPDU().setErrorStatus((byte)1, 0x00);
    m.getPDU().setErrorIdx((byte)1, 0x00);

    m.getPDU().getVarbindList().setLength(1);

    Varbind vb = new Varbind();
    vb.variable = new Data();
    vb.variable.setOID((byte)8, "41.6.1.2.1.1.1.0");
    vb.value = new Data();
    vb.value.setNull();

    m.getPDU().getVarbindList().setVarbindAt(0, vb);

    buff = new byte[m.getLength()];
    m.toBytes(buff, 0);

    dp = new DatagramPacket(buff, buff.length, deviceAddr, devicePort);
    try {
      sock.send(dp);
      sock.receive(dp_recv);
    } catch (Exception e) { }
    
    Log.d("[SnmpManager]", Arrays.toString(buff_recv)); 

    Message m_recv = new Message();

    m_recv.fromBytes(buff_recv, 0);
    vb = m_recv.getPDU().getVarbindList().getVarbindAt(0);

    Log.d("[SnmpManager]", Arrays.toString(vb.variable.value)); 
    Log.d("[SnmpManager]", Arrays.toString(vb.value.value)); 
  }

  public void Set() {}
  public void Walk() {}
}
