package com.clustercontrol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;


public class BinaryUtilTest {

	@Test public void testInitByteList() {
		Byte bite = new Byte("A".getBytes()[0]);
		List<Byte> bytes = BinaryUtil.initByteList(bite, 10);

		assertEquals(bite, bytes.get(0));
		assertEquals(10, bytes.size());
    }
}
