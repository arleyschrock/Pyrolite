package net.razorvine.pickle.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.razorvine.pickle.IObjectConstructor;
import net.razorvine.pickle.PickleException;
import net.razorvine.pickle.PythonException;
import net.razorvine.pickle.Unpickler;
import net.razorvine.pyro.PyroProxy;
import net.razorvine.pyro.PyroURI;
import net.razorvine.pyro.serializer.PickleSerializer;
import net.razorvine.pyro.serializer.PyroSerializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for some more complex unpickler objects (PyroProxy).
 *  
 * @author Irmen de Jong (irmen@razorvine.net)
 */
@SuppressWarnings({"unchecked", "serial"})
public class UnpicklerComplexTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testPickleUnpickleURI() throws IOException {
		PyroURI uri=new PyroURI("PYRO:test@localhost:9999");
		PyroSerializer ser = new PickleSerializer();
		byte[] pickled_uri=ser.serializeData(uri);
		PyroURI uri2=(PyroURI) ser.deserializeData(pickled_uri);
		assertEquals(uri,uri2);

		uri=new PyroURI();
		pickled_uri=ser.serializeData(uri);
		uri2=(PyroURI) ser.deserializeData(pickled_uri);
		assertEquals(uri,uri2);
	}

	@Test
	public void testPickleUnpickleProxy() throws IOException {
		PyroProxy proxy=new PyroProxy("hostname",9999,"objectid");
		proxy.pyroHmacKey = "secret".getBytes();
		PyroSerializer ser = new PickleSerializer();
		byte[] pickled_proxy=ser.serializeData(proxy);
		PyroProxy result = (PyroProxy) ser.deserializeData(pickled_proxy);
		assertEquals(proxy.hostname, result.hostname);
		assertEquals(proxy.objectid, result.objectid);
		assertEquals(proxy.port, result.port);
		assertArrayEquals("secret".getBytes(), result.pyroHmacKey);
	}

	@Test
	public void testUnpickleRealProxy() throws IOException {
		byte[] pickled_proxy=new byte[]
				{-128, 3, 99, 80, 121, 114, 111, 52, 46, 99, 111, 114, 101, 10, 80, 114, 111, 120, 121, 10, 113, 0, 41, -127, 113, 1, 40, 99, 80, 121, 114, 111, 52, 46, 99, 111, 114, 101, 10, 85, 82, 73, 10, 113, 2, 41, -127, 113, 3, 40, 88, 4, 0, 0, 0, 80, 89, 82, 79, 113, 4, 88, 15, 0, 0, 0, 80, 121, 114, 111, 46, 78, 97, 109, 101, 83, 101, 114,
				 118, 101, 114, 113, 5, 78, 88, 9, 0, 0, 0, 108, 111, 99, 97, 108, 104, 111, 115, 116, 113, 6, 77, -126, 35, 116, 113, 7, 98, 99, 98, 117, 105, 108, 116, 105, 110, 115, 10, 115, 101, 116, 10, 113, 8, 93, 113, 9, 40, 88, 2,
				 0, 0, 0, 111, 49, 113, 10, 88, 2, 0, 0, 0, 111, 50, 113, 11, 101, -123, 113, 12, 82, 113, 13, 104, 8, 93, 113,
				 14, 40, 88, 6, 0, 0, 0, 108, 111, 111, 107, 117, 112, 113, 15, 88, 8, 0, 0, 0, 114, 101, 103, 105, 115, 116, 101, 114, 113, 16, 88, 6, 0, 0, 0, 114, 101, 109, 111, 118, 101, 113, 17, 88, 4, 0, 0, 0, 108, 105, 115, 116, 113, 18, 88, 4, 0, 0, 0, 112, 105, 110, 103, 113, 19, 88, 5, 0, 0, 0, 99, 111, 117, 110, 116, 113, 20, 101, -123,
				 113, 21, 82, 113, 22, 104, 8, 93, 113, 23, 40, 88, 5, 0, 0, 0, 97, 116, 116, 114, 49, 113, 24, 88, 5, 0, 0, 0,
				 97, 116, 116, 114, 50, 113, 25, 101, -123, 113, 26, 82, 113, 27, 71, 0, 0, 0, 0, 0, 0, 0, 0, 67, 6, 115, 101, 99, 114, 101, 116, 113, 28, 116, 113, 29, 98, 46};
		
		PyroSerializer ser = new PickleSerializer();
		PyroProxy proxy=(PyroProxy)ser.deserializeData(pickled_proxy);
		assertEquals("Pyro.NameServer",proxy.objectid);
		assertEquals("localhost",proxy.hostname);
		assertEquals(9090,proxy.port);
		assertArrayEquals("secret".getBytes(), proxy.pyroHmacKey);
		
		Set<String> expectedSet = new HashSet<String>();
		expectedSet.add("attr1");
		expectedSet.add("attr2");
		assertEquals(expectedSet, proxy.pyroAttrs);
		expectedSet.clear();
		expectedSet.add("lookup");
		expectedSet.add("ping");
		expectedSet.add("register");
		expectedSet.add("remove");
		expectedSet.add("list");
		expectedSet.add("count");
		assertEquals(expectedSet, proxy.pyroMethods);
		expectedSet.clear();
		expectedSet.add("o1");
		expectedSet.add("o2");
		assertEquals(expectedSet, proxy.pyroOneway);
	}
	
			 
	@Test
	public void testUnpickleMemo() throws PickleException, IOException {
		// the pickle is of the following list: [65, 'hello', 'hello', {'recurse': [...]}, 'hello']
		// i.e. the 4th element is a dict referring back to the list itself and the 'hello' strings are reused
		byte[] pickle = new byte[]
			{(byte) 128, 2, 93, 113, 0, 40, 75, 65, 85, 5, 104, 101, 108, 108, 111, 113, 1, 104, 1, 125, 113, 2,
			85, 7, 114, 101, 99, 117, 114, 115, 101, 113, 3, 104, 0, 115, 104, 1, 101, 46};
		PyroSerializer ser = new PickleSerializer();
		ArrayList<Object> a = (ArrayList<Object>) ser.deserializeData(pickle);
		assertEquals(5, a.size());
		assertEquals(65, a.get(0));
		assertEquals("hello", a.get(1));
		assertSame(a.get(1), a.get(2));
		assertSame(a.get(1), a.get(4));
		HashMap<String, Object> h = (HashMap<String,Object>) a.get(3);
		assertSame(a, h.get("recurse"));
	}
	
	@Test
	public void testUnpickleUnsupportedClass() throws IOException {
		// an unsupported class is mapped to a dictionary containing the class's attributes, and a __class__ attribute with the name of the class
		byte[] pickled = new byte[] {
				(byte)128, 2, 99, 95, 95, 109, 97, 105, 110, 95, 95, 10, 67, 117, 115, 116, 111, 109, 67, 108,
				97, 115, 115, 10, 113, 0, 41, (byte)129, 113, 1, 125, 113, 2, 40, 85, 3, 97, 103, 101, 113, 3,
				75, 34, 85, 6, 118, 97, 108, 117, 101, 115, 113, 4, 93, 113, 5, 40, 75, 1, 75, 2, 75, 3,
				101, 85, 4, 110, 97, 109, 101, 113, 6, 85, 5, 72, 97, 114, 114, 121, 113, 7, 117, 98, 46};

		PyroSerializer ser = new PickleSerializer();
		Map<String, Object> o = (Map<String, Object>) ser.deserializeData(pickled);
		assertEquals(4, o.size());
		assertEquals("Harry", o.get("name"));
		assertEquals(34, o.get("age"));
		ArrayList<Object> expected = new ArrayList<Object>() {{
			add(1);
			add(2);
			add(3);
		}};
		assertEquals(expected, o.get("values"));
		assertEquals("__main__.CustomClass", o.get("__class__"));
	}

	
	public class CustomClazz {
		public String name;
		public int age;
		public ArrayList<Object> values;
		public CustomClazz() 
		{
			
		}
		public CustomClazz(String name, int age, ArrayList<Object> values)
		{
			this.name=name;
			this.age=age;
			this.values=values;
		}
		
		/**
		 * called by the Unpickler to restore state.
		 */
		public void __setstate__(HashMap<String, Object> args) {
			this.name = (String) args.get("name");
			this.age = (Integer) args.get("age");
			this.values = (ArrayList<Object>) args.get("values");
		}			
	}
	
	class CustomClazzConstructor implements IObjectConstructor
	{
		public Object construct(Object[] args) throws PickleException
		{
			if(args.length==0)
			{
				return new CustomClazz();    // default constructor
			}
			else if(args.length==3)
			{
				String name = (String)args[0];
				int age = (Integer) args[1];
				ArrayList<Object> values = (ArrayList<Object>) args[2];
				return new CustomClazz(name, age, values);
			}
			else throw new PickleException("expected 0 or 3 constructor arguments");
		}
	}

	@Test
	public void testUnpickleCustomClass() throws IOException {
		byte[] pickled = new byte[] {
				(byte)128, 2, 99, 95, 95, 109, 97, 105, 110, 95, 95, 10, 67, 117, 115, 116, 111, 109, 67, 108,
				97, 122, 122, 10, 113, 0, 41, (byte)129, 113, 1, 125, 113, 2, 40, 85, 3, 97, 103, 101, 113, 3,
				75, 34, 85, 6, 118, 97, 108, 117, 101, 115, 113, 4, 93, 113, 5, 40, 75, 1, 75, 2, 75, 3,
				101, 85, 4, 110, 97, 109, 101, 113, 6, 85, 5, 72, 97, 114, 114, 121, 113, 7, 117, 98, 46};
		
		Unpickler.registerConstructor("__main__","CustomClazz", new CustomClazzConstructor());
		PyroSerializer ser = new PickleSerializer();
		CustomClazz o = (CustomClazz) ser.deserializeData(pickled);
		assertEquals("Harry" ,o.name);
		assertEquals(34 ,o.age);
		ArrayList<Object> expected = new ArrayList<Object>() {{
			add(1);
			add(2);
			add(3);
		}};
		assertEquals(expected, o.values);
	}
	
	@Test
	public void testUnpickleException() throws IOException {
		PyroSerializer ser = new PickleSerializer();

		// python 2.x
		PythonException x = (PythonException) ser.deserializeData("cexceptions\nZeroDivisionError\np0\n(S'hello'\np1\ntp2\nRp3\n.".getBytes());
		assertEquals("hello", x.getMessage());
		// python 3.x
		x = (PythonException) ser.deserializeData("c__builtin__\nZeroDivisionError\np0\n(Vhello\np1\ntp2\nRp3\n.".getBytes());
		assertEquals("hello", x.getMessage());
		x = (PythonException) ser.deserializeData("cbuiltins\nZeroDivisionError\np0\n(Vhello\np1\ntp2\nRp3\n.".getBytes());
		assertEquals("hello", x.getMessage());

		// python 2.x
		x = (PythonException) ser.deserializeData("cexceptions\nGeneratorExit\np0\n(tRp1\n.".getBytes());
		assertNull(x.getMessage());
		// python 3.x
		x = (PythonException) ser.deserializeData("c__builtin__\nGeneratorExit\np0\n(tRp1\n.".getBytes());
		assertNull(x.getMessage());
		x = (PythonException) ser.deserializeData("cbuiltins\nGeneratorExit\np0\n(tRp1\n.".getBytes());
		assertNull(x.getMessage());
	}
}