package com.benayn;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Sources;
import com.benayn.ustyle.base.AbstractTest;
import com.benayn.ustyle.base.Domain;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class JsonTest extends AbstractTest {
	
	@Test
	public void testJson2() {
		String json = "{\"success\":true,\"code\":0,\"data\":{\"items\":["
				
				+ "{\"id\":47210,\"eventCode\":1013,\"score\":-300,\"scoreDesc\":\"dddddddd\",\"createTime\":1384255106000},"
				+ "{\"id\":47209,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384254931000},"
				
				+ "{\"id\":47207,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384254765000},"
				+ "{\"id\":47105,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384174213000},"
				
				+ "{\"id\":47084,\"eventCode\":1013,\"score\":-300,\"scoreDesc\":\"dddddddd\",\"createTime\":1384156268000},"
				+ "{\"id\":47083,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384156259000},"
				
				+ "{\"id\":47082,\"eventCode\":1013,\"score\":-300,\"scoreDesc\":\"dddddddd\",\"createTime\":1384156244000},"
				+ "{\"id\":47080,\"eventCode\":1013,\"score\":-300,\"scoreDesc\":\"dddddddd\",\"createTime\":1384155858000},"
				
				+ "{\"id\":47075,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384153007000},"
				+ "{\"id\":47073,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384152512000},"
				
				+ "{\"id\":47070,\"eventCode\":1013,\"score\":-300,\"scoreDesc\":\"dddddddd\",\"createTime\":1384151979000},"
				+ "{\"id\":47069,\"eventCode\":1011,\"score\":-200,\"scoreDesc\":\"qiyehuo\",\"createTime\":1384151757000}"
				
				+ "],\"count\":22,\"perPage\":12,\"currentPage\":1,\"maxPage\":2,\"startIndex\":0}}";
		
		log.info(json);
		log.info(JsonW.fmtJson(json));
		
		Map<String, Object> tierM = JsonR.of(json).mapper().tierKey().deepLook().map(); 
		Object[] l = (Object[]) tierM.get("data.items");
		
		assertEquals(l, tierM.get("items"));
		assertEquals(12, l.length);
		assertEquals(22L, tierM.get("count"));
		
		@SuppressWarnings("unchecked")
		Map<String, Object> el1 = (Map<String, Object>) l[0];
		assertEquals(47210L, el1.get("id"));
		
		assertEquals(json, JsonW.of(tierM).asJson());
	}
	
	@Test
	public void testJson3() {
		Domain d = Domain.getDomain();
		
//		d.setsDomains(null);
		
		String json = JsonW.toJson(d);
		log.info(json);
		
//		JsonR.of(json).mapper().info();
		
		Domain d2 = JsonR.of(json).asObject(Domain.class);
		
		String json2 = JsonW.toJson(d2);
		log.info(json2);
		
		assertTrue(d.equals(d2));
		Objects2.isEqual(d, d2);
	}
	
	@Test
	public void testJson() throws IOException {
		//Mapper.from(JsonReader.jsonToMaps(json)).info();
		String json = Sources.asString(JsonTest.class, "/test.json");
		log.info(json);
		
		JsonR.of(json).mapper().info();
		JsonR.of(json).gather().info();
		
		String json2 = Sources.asString(JsonTest.class, "/test2.json");
		log.info(JsonR.of(json2).list());
		
		//json
		Domain d = Domain.getDomain();
		String json3 = JsonWriter.objectToJson(d);
		log.info(json3);
		Domain d2 = (Domain) JsonReader.jsonToJava(json3);
		assertTrue(Objects2.isEqual(d, d2));
		
		Domain d4 = Reflecter.from(d).copyTo(Domain.class);
		assertTrue(Objects2.isEqual(d, d4));
		
		Object[] o = new Object[]{false, true, false};
		Boolean[] b = Arrays2.convert(o, Boolean.class);
		boolean[] b2 = Arrays2.unwrap(Arrays2.convert(o, Boolean.class));
		log.info(b);
		log.info(b2);
	}

}
