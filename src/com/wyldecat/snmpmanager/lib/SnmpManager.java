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
      _sock = new DatagramSocket();
      _deviceAddr = InetAddress.getByName(addr);
    } catch (Exception ignore) { }

    sock = _sock;
    deviceAddr = _deviceAddr;
    devicePort = port;
  }
  
  public void Get() {
    byte buff[]; 
    Packet p = new Packet();

    p.pdu_type = Packet.PDUType.GET_REQUEST;

    p.request_id = new Data(Data.Type.INTEGER, (byte)0x04);

    p.error_status = new Data(Data.Type.INTEGER, (byte)0x01);
    p.error_status.value[0] = 0x00;

    p.error_idx = new Data(Data.Type.INTEGER, (byte)0x01);
    p.error_idx.value[0] = 0x00;

    p.varbind_list = new VarbindList();
    p.varbind_list.varbinds = new Varbind[1];
    p.varbind_list.varbinds[0] = new Varbind();
    p.varbind_list.varbinds[0].variable =
      new Data(Data.Type.OBJECT_IDENTIFIER, (byte)0x09);

    p.varbind_list.varbinds[0].variable.value[0] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[1] = (byte)0x03;
    p.varbind_list.varbinds[0].variable.value[2] = (byte)0x06;
    p.varbind_list.varbinds[0].variable.value[3] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[4] = (byte)0x02;
    p.varbind_list.varbinds[0].variable.value[5] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[6] = (byte)0x07;
    p.varbind_list.varbinds[0].variable.value[7] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[8] = (byte)0x00;

    p.varbind_list.varbinds[0].value =
      new Data(Data.Type.NULL, (byte)0x00);

    buff = new byte[p.getLength()];
    p.toBytes(buff, 0);
  }

  public void Set() {}
  public void Walk() {}
}
