// SnmpManager.java

package com.wyldecat.snmpmanager.lib;

import java.net.*;
import com.wyldecat.snmpmanager.lib.SnmpSchema.Data;

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
  
  void Get() {}
  void Set() {}
  void Walk() {}
}
