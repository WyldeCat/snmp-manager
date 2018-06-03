// Integer32.java

package com.wyldecat.snmpmanager.lib.schema;

import java.io.OutputStream;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import com.wyldecat.snmpmanager.lib.schema.Variable;

public class Integer32 extends Variable {

  public int value;

  public Integer32(int value, int length) {
    this.value = value;
    this.length = length;
  }

  public void decodeBER(BERInputStream bis) throws IOException { }
  public void encodeBER(OutputStream os) throws IOException { }
}

