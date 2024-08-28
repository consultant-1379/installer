package com.distocraft.dc5000.install.ant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.distocraft.dc5000.repository.dwhrep.Aggregation;
import com.distocraft.dc5000.repository.dwhrep.Aggregationrule;
import com.distocraft.dc5000.repository.dwhrep.AggregationruleFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhour;
import com.distocraft.dc5000.repository.dwhrep.BusyhourFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourmapping;
import com.distocraft.dc5000.repository.dwhrep.BusyhourmappingFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourplaceholders;
import com.distocraft.dc5000.repository.dwhrep.BusyhourplaceholdersFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourrankkeys;
import com.distocraft.dc5000.repository.dwhrep.BusyhourrankkeysFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhoursource;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;


import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

@RunWith (JMock.class)
public class HandleBusyhourActivationTest {
	private Mockery context = new JUnit4Mockery();
	private RockFactory rock;
	private String testDbURL      = "jdbc:hsqldb:mem:dwhrep";
	private String testDriverName = "org.hsqldb.jdbcDriver";
    private String testUserName   = "SA";
    private String testPassword   = "";
	private String techPackName   = "DC_E_RBS";
	private String techPackVersionID = "DC_E_RBS:((123))";
	
    private Field  dwhrepRockFactoryField;
    private Field  techPackVersionIDField;
    private Field  techPackNameField;
    private Method updateBusyhourEnableFlagsMethod;
    private Method copyCustomBusyhoursMethod;
    private Method copyCustomBusyhourmappingMethod;
    private Method copyCustomBusyhourRankkeysMethod;
    private Method copyCustomBusyhourSourceMethod;
    private Method updateBusyhourPlaceholdersMethod;
    private Method updateAggregationRulesMethod;
    private Method updateProductBHAggregationRulesMethod;
    private Method copyCustomBHAggregationRulesMethod;
    private Method checkBusyhourPlaceholdersExist;
    
    private HandleBusyhourActivation handleBusyhourActivation;
    
    
    @Before
    public void setup(){
    	try {
    		handleBusyhourActivation = new HandleBusyhourActivation();
			rock = new RockFactory(testDbURL, testUserName, testPassword, testDriverName, "test", true);
			
			dwhrepRockFactoryField = HandleBusyhourActivation.class.getDeclaredField("dwhrepRockFactory");
			dwhrepRockFactoryField.setAccessible(true);
			dwhrepRockFactoryField.set(handleBusyhourActivation, rock);
			
			techPackNameField = HandleBusyhourActivation.class.getDeclaredField("techPackName");
			techPackNameField.setAccessible(true);
			techPackNameField.set(handleBusyhourActivation, techPackName);

			techPackVersionIDField = HandleBusyhourActivation.class.getDeclaredField("techPackVersionID");
			techPackVersionIDField.setAccessible(true);
			techPackVersionIDField.set(handleBusyhourActivation, techPackVersionID);

			updateBusyhourEnableFlagsMethod = HandleBusyhourActivation.class.getDeclaredMethod("updateBusyhourEnableFlags", null);
			updateBusyhourEnableFlagsMethod.setAccessible(true);

			copyCustomBusyhoursMethod = HandleBusyhourActivation.class.getDeclaredMethod("copyCustomBusyhours", null);
			copyCustomBusyhoursMethod.setAccessible(true);

			copyCustomBusyhourmappingMethod = HandleBusyhourActivation.class.getDeclaredMethod("copyCustomBusyhourmapping", null);
			copyCustomBusyhourmappingMethod.setAccessible(true);

			copyCustomBusyhourRankkeysMethod = HandleBusyhourActivation.class.getDeclaredMethod("copyCustomBusyhourRankkeys", null);
			copyCustomBusyhourRankkeysMethod.setAccessible(true);

			copyCustomBusyhourSourceMethod = HandleBusyhourActivation.class.getDeclaredMethod("copyCustomBusyhourSource", null);
			copyCustomBusyhourSourceMethod.setAccessible(true);
			
			updateBusyhourPlaceholdersMethod = HandleBusyhourActivation.class.getDeclaredMethod("updateBusyhourPlaceholders", null);
			updateBusyhourPlaceholdersMethod.setAccessible(true);
			
			updateAggregationRulesMethod = HandleBusyhourActivation.class.getDeclaredMethod("updateAggregationRules", null);
			updateAggregationRulesMethod.setAccessible(true);
			
			updateProductBHAggregationRulesMethod = HandleBusyhourActivation.class.getDeclaredMethod("updateProductBHAggregationRules", String.class);
			updateProductBHAggregationRulesMethod.setAccessible(true);
			
			copyCustomBHAggregationRulesMethod = HandleBusyhourActivation.class.getDeclaredMethod("copyCustomBHAggregationRules", String.class);
			copyCustomBHAggregationRulesMethod.setAccessible(true);
			
			checkBusyhourPlaceholdersExist = HandleBusyhourActivation.class.getDeclaredMethod("checkBusyhourPlaceholdersExist", null);
			checkBusyhourPlaceholdersExist.setAccessible(true);
			DatabaseTestUtils.loadSetup(rock, "HandleBusyhourActivation");
		} catch (Exception e) {
			fail("Setup didn't work..."+e.getMessage());
		}
    }
 
	@After
	public void tearDown() throws Exception{
		DatabaseTestUtils.shutdown(rock);
	}
	
	@Test
	public void testDispose(){
		//need to get the targetVersionID. This can be obtained from the Busyhour table.
		final Busyhour busyhour = new Busyhour(rock);
		busyhour.setVersionid(techPackVersionID);
		busyhour.setBhlevel("DC_E_RBS_ULBASEBANDPOOLBH");
		busyhour.setBhtype("CP0");
		try {
			final BusyhourFactory busyhourFactory = new BusyhourFactory(rock, busyhour);
			System.out.println("Size = "+busyhourFactory.size());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    @Test
    public void updateProductBusyhourActivationStates(){
    	try {
			int actual = (Integer) updateBusyhourEnableFlagsMethod.invoke(handleBusyhourActivation, null);
			assertEquals(2, actual);

			// fetch the installed (new) techpacks product busyhours
			final Busyhour installedbh = new Busyhour(rock);
			installedbh.setVersionid(techPackVersionID);
			installedbh.setPlaceholdertype("PP");
			BusyhourFactory installedbhF = new BusyhourFactory(rock, installedbh);
			for(Busyhour bh:installedbhF.get()){
				if(bh.getBhlevel().equalsIgnoreCase("PP0")){
					assertTrue(bh.getEnable() == 0); //this is disabled from the old TP.
				}
				if(bh.getBhlevel().equalsIgnoreCase("PP1")){
					assertTrue(bh.getEnable() == 1); //this is enabled in the new TP.
				}
				if(bh.getBhlevel().equalsIgnoreCase("PP2")){
					assertTrue(bh.getEnable() == 1); //this is enabled in the new TP.
				}
			}
			

		} catch (IllegalArgumentException e) {
			fail("Could not call updateBusyhourEnableFlagsMethod (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call updateBusyhourEnableFlagsMethod (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call updateBusyhourEnableFlagsMethod (3)..."+e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void copyCustomBusyhours(){
    	try {
			int actual = (Integer) copyCustomBusyhoursMethod.invoke(handleBusyhourActivation, null);
			
			// fetch the installed (new) techpacks product busyhours
			final Busyhour installedbh = new Busyhour(rock);
			installedbh.setVersionid(techPackVersionID);
			installedbh.setPlaceholdertype("CP");
			BusyhourFactory installedbhF = new BusyhourFactory(rock, installedbh);
			assertEquals(15, installedbhF.size()); //total number of CP's should be 5.
			for(Busyhour bh:installedbhF.get()){
				if(bh.getBhtype().equals("CP0") && bh.getBhlevel().equals("DC_E_RBS_ULBASEBANDPOOLBH")){
					assertTrue(bh.getBhcriteria().equals("CP SQl is here0"));
				}
				if(bh.getBhtype().equals("CP1") && bh.getBhlevel().equals("DC_E_RBS_ULBASEBANDPOOLBH")){
					assertTrue(bh.getBhcriteria().equals("CP SQl is here1"));
				}
				if(bh.getBhtype().equals("CP2") && bh.getBhlevel().equals("DC_E_RBS_ULBASEBANDPOOLBH")){
					assertTrue(bh.getBhcriteria().equals(""));
				}
			}
			assertEquals(2, actual);
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhours (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhours (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhours (3)..."+e.getMessage());
		} catch (SQLException e) {
			fail("SQL Exception..."+e.getMessage());
		} catch (RockException e) {
			fail("Rock Exception..."+e.getMessage());
		}
    }

    @Test
    public void copyCustomBusyhourmapping(){
    	try {
			int actual = (Integer) copyCustomBusyhourmappingMethod.invoke(handleBusyhourActivation, null);
			
			// fetch the installed (new) techpacks custom busyhourmapping
			final Busyhourmapping installedbh = new Busyhourmapping(rock);
			installedbh.setVersionid(techPackVersionID);
			BusyhourmappingFactory installedbhF = new BusyhourmappingFactory(rock, installedbh);
			assertEquals(60, installedbhF.size()); //total number of PP's CP's should be 60.
			
			for(Busyhourmapping bhMapping: installedbhF.get()){
				if(bhMapping.getBhtype().startsWith("CP")){
					assertTrue(bhMapping.getEnable() == 0);
				}
			}
			assertEquals(30, actual);
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhourmapping (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhourmapping (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhourmapping (3)..."+e.getMessage());
		} catch (SQLException e) {
			fail("SQL Exception..."+e.getMessage());
		} catch (RockException e) {
			fail("Rock Exception..."+e.getMessage());
		}
    }
 
    @Test
    public void copyCustomBusyhourRankkeys(){
    	try {
//    		copyCustomBusyhoursMethod.invoke(handleBusyhourActivation, null);
    		int actual = (Integer) copyCustomBusyhourRankkeysMethod.invoke(handleBusyhourActivation, null);
			
			// fetch the installed (new) techpacks custom busyhourmapping
			final Busyhourrankkeys installedbh = new Busyhourrankkeys(rock);
			installedbh.setVersionid(techPackVersionID);
			BusyhourrankkeysFactory installedbhF = new BusyhourrankkeysFactory(rock, installedbh);
			assertEquals(20, installedbhF.size()); //total number of PP's & CP's should be 20.
			
			for(Busyhourrankkeys bhRankkeys: installedbhF.get()){
				if(bhRankkeys.getBhtype().startsWith("CP")){
					assertTrue(bhRankkeys.getOrdernbr() == 99);
				}
			}
			assertEquals(10, actual);
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhourmapping (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhourmapping (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhourmapping (3)..."+e.getMessage());
		} catch (SQLException e) {
			fail("SQL Exception..."+e.getMessage());
		} catch (RockException e) {
			fail("Rock Exception..."+e.getMessage());
		}
    }

    @Test
    public void copyCustomBusyhourSource(){
    	try {
			int actual = (Integer) copyCustomBusyhourSourceMethod.invoke(handleBusyhourActivation, null);
			assertEquals(3, actual);
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhourmapping (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhourmapping (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhourmapping (3)..."+e.getMessage());
		}
    }

    @Test
    public void updateProductBHAggregationRules(){
    	String oldVersionId = "DC_E_RBS:((122))";
    	try {
			int actual = (Integer)updateProductBHAggregationRulesMethod.invoke(handleBusyhourActivation, oldVersionId);
			assertEquals(3, actual);
			
			final Aggregationrule newAggregationRuleSearch = new Aggregationrule(rock);
			newAggregationRuleSearch.setVersionid(techPackVersionID);
			newAggregationRuleSearch.setTarget_level("RANKBH");
			AggregationruleFactory newAggregationRuleFactory = new AggregationruleFactory(
					rock, newAggregationRuleSearch);
			
			//there should be 18 AggregationRules entries.
			assertEquals(18, newAggregationRuleFactory.size());
			for(Aggregationrule newAggregationRule: newAggregationRuleFactory.get()){
				if(newAggregationRule.getAggregation().startsWith("DC_E_RBS_DLBASEBANDPOOLBH_") && 
						newAggregationRule.getAggregation().endsWith("RANKBH_DLBASEBANDPOOL_PP0")){
					assertTrue(newAggregationRule.getEnable() == 0);
				}
				if(newAggregationRule.getAggregation().startsWith("DC_E_RBS_DLBASEBANDPOOLBH_") && 
						newAggregationRule.getAggregation().endsWith("RANKBH_DLBASEBANDPOOL_PP1")){
					assertTrue(newAggregationRule.getEnable() == 1);
				}
				if(newAggregationRule.getAggregation().startsWith("DC_E_RBS_DLBASEBANDPOOLBH_") && 
						newAggregationRule.getAggregation().endsWith("RANKBH_DLBASEBANDPOOL_PP2")){
					assertTrue(newAggregationRule.getEnable() == 0);
				}
			}
		} catch (IllegalArgumentException e) {
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			fail(e.getMessage());
		} catch (SQLException e) {
			fail(e.getMessage());
		} catch (RockException e) {
			fail(e.getMessage());
		}
    }
    
    @Test
    public void copyCustomBHAggregationRules(){
    	String oldVersionId = "DC_E_RBS:((122))";
    	try {
			int actual = (Integer)copyCustomBHAggregationRulesMethod.invoke(handleBusyhourActivation, oldVersionId);
			assertEquals(6, actual);
			
			final Aggregationrule newAggregationRuleSearch = new Aggregationrule(rock);
			newAggregationRuleSearch.setVersionid(techPackVersionID);
			newAggregationRuleSearch.setTarget_level("RANKBH");
			AggregationruleFactory newAggregationRuleFactory = new AggregationruleFactory(
					rock, newAggregationRuleSearch);
			
			//there should be 18 AggregationRules entries.
			assertEquals(18, newAggregationRuleFactory.size());
			for(Aggregationrule newAggregationRule: newAggregationRuleFactory.get()){
				if(newAggregationRule.getAggregation().startsWith("DC_E_RBS_DLBASEBANDPOOLBH_") && 
						newAggregationRule.getAggregation().endsWith("RANKBH_DLBASEBANDPOOL_CP0")){
					assertTrue(newAggregationRule.getAggregation()+ " ENABLED = "+newAggregationRule.getEnable(), newAggregationRule.getEnable() == 0);
					assertTrue(newAggregationRule.getAggregation()+ " Source_mtableid = "+newAggregationRule.getSource_mtableid(), newAggregationRule.getSource_mtableid().equals("DC_E_RBS:((123)):DC_E_RBS_DLBASEBANDPOOLBH:RANKBH"));
				}
				if(newAggregationRule.getAggregation().startsWith("DC_E_RBS_DLBASEBANDPOOLBH_") && 
						newAggregationRule.getAggregation().endsWith("RANKBH_DLBASEBANDPOOL_CP1")){
					assertTrue(newAggregationRule.getAggregation()+ " ENABLED = "+newAggregationRule.getEnable(), newAggregationRule.getEnable() == 1);
					assertTrue(newAggregationRule.getAggregation()+ " Source_mtableid = "+newAggregationRule.getSource_mtableid(), newAggregationRule.getSource_mtableid().equals("DC_E_RBS:((123)):DC_E_RBS_DLBASEBANDPOOLBH:RANKBH"));
				}
				if(newAggregationRule.getAggregation().equals("DC_E_RBS_DLBASEBANDPOOLBH_DAYRANKBH_DLBASEBANDPOOL_CP2")){
					assertTrue(newAggregationRule.getAggregation()+ " ENABLED = "+newAggregationRule.getEnable(), newAggregationRule.getEnable() == 0);
					assertTrue(newAggregationRule.getAggregation()+ " Source_mtableid = "+newAggregationRule.getSource_mtableid(), newAggregationRule.getSource_mtableid().equals(""));
				}
			}
		} catch (IllegalArgumentException e) {
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			fail(e.getMessage());
		} catch (SQLException e) {
			fail(e.getMessage());
		} catch (RockException e) {
			fail(e.getMessage());
		}
    }

    
    
    @Test
    public void copyCustomBusyhourPlaceholders(){
    	try {
			int actual = (Integer) updateBusyhourPlaceholdersMethod.invoke(handleBusyhourActivation, null);
			assertEquals(5, actual);

			// fetch the installed (new) techpacks custom busyhourmapping
			final Busyhourplaceholders installedbh = new Busyhourplaceholders(rock);
			installedbh.setVersionid(techPackVersionID);
			BusyhourplaceholdersFactory installedbhF = new BusyhourplaceholdersFactory(rock, installedbh);
			
			for(Busyhourplaceholders bhRankkeys: installedbhF.get()){
					assertTrue(bhRankkeys.getCustomplaceholders() == 99);
			}
			
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhourmapping (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhourmapping (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhourmapping (3)..."+e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Test
    public void testCheckBusyhourExists(){
    	try {
			boolean answer = (Boolean)checkBusyhourPlaceholdersExist.invoke(handleBusyhourActivation, null);
			assertTrue(answer);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void testCheckBusyhourNotExists(){
    	try {
			techPackNameField.set(handleBusyhourActivation, "DC_E_CMN_STS");
			techPackVersionIDField.set(handleBusyhourActivation, "DC_E_CMN_STS:((10))");
			boolean answer = (Boolean)checkBusyhourPlaceholdersExist.invoke(handleBusyhourActivation, null);
			assertFalse(answer);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Test
    public void copyCustomBusyhourPlaceholdersPreviousNotExist(){
    	try {
			techPackNameField.set(handleBusyhourActivation, "DC_E_CMN_STS");
			techPackVersionIDField.set(handleBusyhourActivation, "DC_E_CMN_STS:((10))");

			int actual = (Integer) updateBusyhourPlaceholdersMethod.invoke(handleBusyhourActivation, null);
			assertEquals(0, actual);
			
		} catch (IllegalArgumentException e) {
			fail("Could not call copyCustomBusyhourmapping (1)..."+e.getMessage());
		} catch (IllegalAccessException e) {
			fail("Could not call copyCustomBusyhourmapping (2)..."+e.getMessage());
		} catch (InvocationTargetException e) {
			fail("Could not call copyCustomBusyhourmapping (3)..."+e.getMessage());
		} 
    }

    @Test
    public void testfindSetterMethodsForBusyhour(){
    	Class<Busyhour> className = Busyhour.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(20, setterMethods.size());
    }
    
    @Test
    public void testfindSetterMethodsForBusyhourmapping(){
    	Class<Busyhourmapping> className = Busyhourmapping.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(9, setterMethods.size());
    }

    @Test
    public void testfindSetterMethodsForAggregation(){
    	Class<Aggregation> className = Aggregation.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(10, setterMethods.size());
    }

    @Test
    public void testfindSetterMethodsForAggregationrule(){
    	Class<Aggregationrule> className = Aggregationrule.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(15, setterMethods.size());
    }

    @Test
    public void testfindSetterMethodsForBusyhoursource(){
    	Class<Busyhoursource> className = Busyhoursource.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(6, setterMethods.size());
    }

    @Test
    public void testfindSetterMethodsForBusyhourrankkeys(){
    	Class<Busyhourrankkeys> className = Busyhourrankkeys.class;
    	ArrayList<Method> setterMethods = handleBusyhourActivation.findSetterMethods(className);
    	Iterator<Method> list = setterMethods.iterator();
    	while(list.hasNext()){
    		String name = list.next().getName();
    		assertFalse("Got setModifiedColumns", name.equalsIgnoreCase("setModifiedColumns"));
    		assertFalse("Got setcolumnsAndSequences", name.equalsIgnoreCase("setcolumnsAndSequences"));
    		assertFalse("Got setDefaults", name.equalsIgnoreCase("setDefaults"));
    		assertFalse("Got setNewItem", name.equalsIgnoreCase("setNewItem"));
    		assertFalse("Got setValidateData", name.equalsIgnoreCase("setValidateData"));
    		assertFalse("Got setOriginal", name.equalsIgnoreCase("setOriginal"));
    	}
    	assertEquals(8, setterMethods.size());
    }

    @Test
    public void testIsPrimaryKeyMatchTrue(){
    	Busyhourmapping oldTable = new Busyhourmapping(rock);
    	oldTable.setVersionid("version1");
    	Busyhourmapping newTable = new Busyhourmapping(rock);
    	newTable.setVersionid("version1");
 
    	boolean result;
		try {
			result = handleBusyhourActivation.isPrimaryKeyMatch(oldTable, newTable);
	    	assertTrue(result);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
    @Test
    public void testIsPrimaryKeyMatchFalse(){
    	Busyhourmapping oldTable = new Busyhourmapping(rock);
    	oldTable.setVersionid("version1");
    	oldTable.setBhlevel("bhlevel1");
    	Busyhourmapping newTable = new Busyhourmapping(rock);
    	newTable.setVersionid("version1");
    	newTable.setBhlevel("bhlevel2");
 
    	boolean result;
		try {
			result = handleBusyhourActivation.isPrimaryKeyMatch(oldTable, newTable);
	    	assertFalse(result);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }

}
