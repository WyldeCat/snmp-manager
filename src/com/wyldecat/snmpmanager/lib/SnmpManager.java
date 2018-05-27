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

    /* Construct Message header */
    m.length = 0;

    m.version = new Data(Data.Type.INTEGER, (byte)0x01);
    m.version.value[0] = 0x01;

    m.community_string = new Data(Data.Type.OCTET_STRING, (byte)0x06);
    m.community_string.value[0] = 0x70;
    m.community_string.value[1] = 0x75;
    m.community_string.value[2] = 0x62;
    m.community_string.value[3] = 0x6c;
    m.community_string.value[4] = 0x69;
    m.community_string.value[5] = 0x63;

    m.pdu = new PDU();

    /* Construct PDU */
    PDU p = m.pdu;
    p.length = 0;

    p.type = PDU.Type.GET_REQUEST;

    p.request_id = new Data(Data.Type.INTEGER, (byte)0x04);
    p.request_id.value[0] = (byte)0x08;
    p.request_id.value[1] = (byte)0x8c;
    p.request_id.value[2] = (byte)0x1e;
    p.request_id.value[3] = (byte)0x3b;

    p.error_status = new Data(Data.Type.INTEGER, (byte)0x01);
    p.error_status.value[0] = 0x00;

    p.error_idx = new Data(Data.Type.INTEGER, (byte)0x01);
    p.error_idx.value[0] = 0x00;

    p.varbind_list = new VarbindList();
    p.varbind_list.varbinds = new Varbind[1];
    p.varbind_list.varbinds[0] = new Varbind();
    p.varbind_list.varbinds[0].variable =
      new Data(Data.Type.OBJECT_IDENTIFIER, (byte)0x08);

    p.varbind_list.varbinds[0].variable.value[0] = (byte)0x2b;
    p.varbind_list.varbinds[0].variable.value[1] = (byte)0x06;
    p.varbind_list.varbinds[0].variable.value[2] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[3] = (byte)0x02;
    p.varbind_list.varbinds[0].variable.value[4] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[5] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[6] = (byte)0x01;
    p.varbind_list.varbinds[0].variable.value[7] = (byte)0x00;

    p.varbind_list.varbinds[0].value =
      new Data(Data.Type.NULL, (byte)0x00);

    buff = new byte[m.getLength()];
    m.toBytes(buff, 0);

    dp = new DatagramPacket(buff, buff.length, deviceAddr, devicePort);
    try {
      sock.send(dp);
      sock.receive(dp_recv);
    } catch (Exception e) {
      Log.d("[SnmpManager]", e.getMessage());
    }

    Log.d("[SnmpManager]", "Here comes packet");
    Log.d("[SnmpManager]", Arrays.toString(dp_recv.getData()));
  }

  public void Set() {}
  public void Walk() {}
}
