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

  /*
   * a ctor of SnmpManager.
   * Set timeout option about 10 secs.
   */
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

  /*
   * Requesting function.
   * Send a SNMP message containing one of GetRequest-PDU,
   * GetNextRequest-PDU or SetRequest-PDU to server. It retransmits message
   * when timeout occur.
   */
  private String request(String oid, Variable val, byte type) throws Exception {
    BEROutputStream bos = new BEROutputStream(ByteBuffer.wrap(buff_send));
    BERInputStream bis = new BERInputStream(ByteBuffer.wrap(buff_recv));
    if (type == (byte)0xa0 || type == (byte)0xa1) {
      m_send.setCommunityString("public");
    } else if(type == (byte)0xa3) {
      m_send.setCommunityString("write");
    }

    m_send.getPDU().setType(type);
    m_send.getPDU().getVarbindList().setVarbindAt(0, new Varbind(
        new OID(oid), val
      )
    );
    m_send.updateLength();
    m_send.encodeBER(bos);

    pkt_send.setLength(m_send.getBERLength());

    int num_retry = 0;
    while (true) {
      sock.send(pkt_send);
      try {
        sock.receive(pkt_recv);
        break;
      } catch (SocketTimeoutException ste) {
        num_retry++;
        if (num_retry == 10) {
          throw ste;
        }
      }
    }

    m_recv.decodeBER(bis);

    return m_recv.getPDU().getVarbindList().getVarbindAt(0).toString();
  }

  /*
   * Sends string object to handler
   */
  private void sendMessage(Handler handler, String str) {
    android.os.Message msg = handler.obtainMessage();
    msg.obj = str;
    handler.sendMessage(msg);
  }

  /*
   * Returns working flag
   */
  public boolean isWorking() {
    return isWorking.get();
  }

  /*
   * Turns working flag on
   */
  public void setIsWorking() {
    isWorking.set(true);
  }

  /*
   * A function for SNMP Get operation.
   * Calls request function and sendMessage with return of the request call
   */
  public void Get(Handler handler, String oid) throws Exception {
    try {
      sendMessage(handler, request(oid, new Null(), (byte)0xa0));
      isWorking.set(false); 
    } catch (Exception e) {
      isWorking.set(false); 
      throw e;
    }
  }

  /*
   * A function for SNMP Walk operation.
   * Keep calling request function till met EndOfMIBView
   */
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

        sendMessage(handler, ret);
        Log.d("[snmp-walk]", ret);

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

  /*
   * A function for SNMP Set operation.
   * Calls request function twice. First call is for checking value's type and
   * Second call is for sending a set message.
   */
  public void Set(Handler handler, String oid, String val) throws Exception {
    try {
      request(oid, new Null(), (byte)0xa0); 
      Variable v = m_recv.getPDU().getVarbindList()
        .getVarbindAt(0).getValue();

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

      sendMessage(handler, request(oid, v, (byte)0xa3));
      int errStatus = m_recv.getPDU().getErrorStatus().value;
      if (errStatus != 0) {
        sendMessage(handler, "Error Status: " + Integer.toString(errStatus));
      }

      isWorking.set(false);
    } catch (Exception e) {
      isWorking.set(false);
      throw e;
    }
  }
}

