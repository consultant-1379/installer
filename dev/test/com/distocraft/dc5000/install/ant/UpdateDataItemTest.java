package com.distocraft.dc5000.install.ant;

import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;

import ssc.rockfactory.RockFactory;

public class UpdateDataItemTest {
	
	private final String driver = "org.hsqldb.jdbcDriver";
	private final String dburl = "jdbc:hsqldb:mem:testdb";
	private final String dbusr = "SA";
	private final String dbpwd = "";
	private UpdateDataItem updateDataItem;
	private Method updateDataItemobj;
	private Method updateDataItemobj1;
	private Field dwhrepRockFactoryObj;
	private Field resultSetObj;
	private RockFactory rock = null;
	private Field stringObj;
	private final String query = "SELECT CURRENT_DATE AS today, CURRENT_TIME AS now FROM (VALUES(0))";
	private Class classObj ;
	private Connection con = null;
	private PreparedStatement ps= null ;
	private ResultSet rs = null;
	private final Logger log = Logger.getAnonymousLogger();
	
	@Before
	public void setUp() {
		updateDataItem = new UpdateDataItem();
		try {
			classObj = updateDataItem.getClass();	
			rock = DatabaseTestUtils.getTestDbConnection();
			con = DriverManager.getConnection(dburl, dbusr, dbpwd);	
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	@Test
	public void testgetDataForDataItemTable() {
		
		try {
			stringObj = classObj.getDeclaredField("selectQuery");
			stringObj.setAccessible(true);
			stringObj.set(updateDataItem, query);
			dwhrepRockFactoryObj = classObj.getDeclaredField("dwhrepRockFactory");
			dwhrepRockFactoryObj.setAccessible(true);
			dwhrepRockFactoryObj.set(updateDataItem, rock);
			resultSetObj = classObj.getDeclaredField("resultSet");
			resultSetObj.setAccessible(true);
			resultSetObj.set(updateDataItem, rs);
			dwhrepRockFactoryObj = classObj.getDeclaredField("dwhrepRockFactory");
			dwhrepRockFactoryObj.setAccessible(true);
			dwhrepRockFactoryObj.set(updateDataItem, rock);			
		    updateDataItemobj = UpdateDataItem.class.getDeclaredMethod("getDataForDataItemTable", new Class[] {});
		    updateDataItemobj.setAccessible(true);
		    updateDataItemobj.invoke(updateDataItem, new Object[] {});	
		}catch(Exception e) {
			e.getMessage();
		}		
	}
	
	
	@Test
	public void testupdateDataItem() {
		Class classObj = updateDataItem.getClass();
		try { 
			stringObj = classObj.getDeclaredField("selectQuery");
			stringObj.setAccessible(true);
			stringObj.set(updateDataItem, query);
			dwhrepRockFactoryObj = classObj.getDeclaredField("dwhrepRockFactory");
			dwhrepRockFactoryObj.setAccessible(true);
			dwhrepRockFactoryObj.set(updateDataItem, rock);	
			resultSetObj = classObj.getDeclaredField("resultSet");
			resultSetObj.setAccessible(true);
			resultSetObj.set(updateDataItem, rs);
			dwhrepRockFactoryObj = classObj.getDeclaredField("dwhrepRockFactory");
			dwhrepRockFactoryObj.setAccessible(true);
			dwhrepRockFactoryObj.set(updateDataItem, rock);					
		    updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("updateDataItem", new Class[] {});
		    updateDataItemobj1.setAccessible(true);
		    updateDataItemobj1.invoke(updateDataItem, new Object[] {});
		}catch(Exception e) {
			e.getMessage();
		}
		
	}
	@Test
	public void closeConnectionTestConNull() {
		try {
		updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("closeConnection", Logger.class,ResultSet.class, Statement.class,Connection.class);
	    updateDataItemobj1.setAccessible(true);
	    con=null;
	    updateDataItemobj1.invoke(updateDataItem, log,rs,ps,con);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void closeConnectionTestRsNull() {
		try {
		updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("closeConnection", Logger.class,ResultSet.class, Statement.class,Connection.class);
	    updateDataItemobj1.setAccessible(true);
	    rs=null;
	    updateDataItemobj1.invoke(updateDataItem, log,rs,ps,con);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void closeConnectionTestStmtNull() {
		try {
		updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("closeConnection", Logger.class,ResultSet.class, Statement.class,Connection.class);
	    updateDataItemobj1.setAccessible(true);
	    ps=null;
	    updateDataItemobj1.invoke(updateDataItem, log,rs,ps,con);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConnectionValidNull() {
		try {
			con=null;			
		    updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("connVaildation", Connection.class);
		    updateDataItemobj1.setAccessible(true);
		   boolean valid=  (boolean) updateDataItemobj1.invoke(updateDataItem,con);
			assertFalse(valid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConnectionValidNotNull() {
		try {
		    updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("connVaildation", Connection.class);
		    updateDataItemobj1.setAccessible(true);
		   boolean valid= (boolean) updateDataItemobj1.invoke(updateDataItem,con);
		   assertTrue(valid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConnectionValidClose() {
		try {
			con.close();
		    updateDataItemobj1 = UpdateDataItem.class.getDeclaredMethod("connVaildation", Connection.class);
		    updateDataItemobj1.setAccessible(true);
		   boolean vaild = (boolean) updateDataItemobj1.invoke(updateDataItem,con);
		   assertFalse(vaild);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
