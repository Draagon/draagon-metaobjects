package com.draagon.meta.util;

import com.draagon.meta.loader.LoaderOptions;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class DataConverterTests {

    @Test
    public void testByteConvert() {
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte((byte)5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte((short)5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte(5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte((long)5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte((float)5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte((double)5));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte(new Date(5)));
        assertEquals( Byte.valueOf((byte)5), DataConverter.toByte("5"));
        assertNull( DataConverter.toByte(""));
    }

    @Test
    public void testByteFails() {
        try {
            DataConverter.toByte(Short.MAX_VALUE);
            fail( "Max value should fail" );
        } catch( NumberFormatException e ) {
            try {
                DataConverter.toByte(Integer.MAX_VALUE);
                fail( "Max value should fail" );
            } catch( NumberFormatException e0 ) {
                try {
                    DataConverter.toByte(Long.MAX_VALUE);
                    fail("Max value should fail");
                } catch (NumberFormatException e1) {
                    try {
                        DataConverter.toByte(Float.MAX_VALUE);
                        fail("Max value should fail");
                    } catch (NumberFormatException e2) {
                        try {
                            DataConverter.toByte(Double.MAX_VALUE);
                            fail("Max value should fail");
                        } catch (NumberFormatException e3) {
                            try {
                                DataConverter.toByte(new Date((long) Byte.MAX_VALUE + 1L));
                                fail("Max value should fail");
                            } catch (NumberFormatException e4) {
                                try {
                                    DataConverter.toByte("fail");
                                    fail("Convert should fail");
                                } catch (NumberFormatException e5) {
                                    try {
                                        DataConverter.toByte(new LoaderOptions());
                                        fail("Convert should fail");
                                    } catch (Exception ex) {
                                        assertTrue(ex instanceof NumberFormatException);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testShortConvert() {
        assertEquals( Short.valueOf((short)5), DataConverter.toShort((byte)5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort((short)5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort(5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort((long)5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort((float)5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort((double)5));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort(new Date(5)));
        assertEquals( Short.valueOf((short)5), DataConverter.toShort("5"));
        assertNull( DataConverter.toShort(""));
    }

    @Test
    public void testShortFails() {
        try {
            DataConverter.toShort(Integer.MAX_VALUE);
            fail( "Max value should fail" );
        } catch( NumberFormatException e0 ) {
            try {
                DataConverter.toShort(Long.MAX_VALUE);
                fail("Max value should fail");
            } catch (NumberFormatException e1) {
                try {
                    DataConverter.toShort(Float.MAX_VALUE);
                    fail("Max value should fail");
                } catch (NumberFormatException e2) {
                    try {
                        DataConverter.toShort(Double.MAX_VALUE);
                        fail("Max value should fail");
                    } catch (NumberFormatException e3) {
                        try {
                            DataConverter.toShort(new Date((long) Short.MAX_VALUE + 1L));
                            fail("Max value should fail");
                        } catch (NumberFormatException e4) {
                            try {
                                DataConverter.toShort("fail");
                                fail("Convert should fail");
                            } catch (NumberFormatException e5) {
                                try {
                                    DataConverter.toShort(new LoaderOptions());
                                    fail("Convert should fail");
                                } catch (Exception ex) {
                                    assertTrue(ex instanceof NumberFormatException);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testIntConvert() {
        assertEquals( Integer.valueOf(5), DataConverter.toInt((byte)5));
        assertEquals( Integer.valueOf(5), DataConverter.toInt((short)5));
        assertEquals( Integer.valueOf(5), DataConverter.toInt(5));
        assertEquals( Integer.valueOf(5), DataConverter.toInt((long)5));
        assertEquals( Integer.valueOf(5), DataConverter.toInt((float)5));
        assertEquals( Integer.valueOf(5), DataConverter.toInt((double)5));
        assertEquals( Integer.valueOf(Integer.MAX_VALUE-100), DataConverter.toInt(new Date(Integer.MAX_VALUE-100)));
        assertEquals( Integer.valueOf(5), DataConverter.toInt("5"));
        assertNull( DataConverter.toInt(""));
    }

    @Test
    public void testIntFails() {
        try {
            DataConverter.toInt(Long.MAX_VALUE);
            fail( "Max value should fail" );
        } catch( NumberFormatException e ) {
            try {
                DataConverter.toInt(Float.MAX_VALUE);
                fail( "Max value should fail" );
            } catch( NumberFormatException e2 ) {
                try {
                    DataConverter.toInt(Double.MAX_VALUE);
                    fail( "Max value should fail" );
                } catch( NumberFormatException e3 ) {
                    try {
                        DataConverter.toInt( new Date( (long) Integer.MAX_VALUE + 1L ));
                        fail( "Max value should fail" );
                    } catch( NumberFormatException e4 ) {
                        try {
                            DataConverter.toInt( "fail");
                            fail( "Convert should fail" );
                        } catch( NumberFormatException e5 ) {
                            try {
                                DataConverter.toInt( new LoaderOptions());
                                fail( "Convert should fail" );
                            } catch( Exception e6 ) {
                                assertTrue( e6 instanceof NumberFormatException );
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testLongConvert() {
        assertEquals( Long.valueOf(5), DataConverter.toLong((byte)5));
        assertEquals( Long.valueOf(5), DataConverter.toLong((short)5));
        assertEquals( Long.valueOf(5), DataConverter.toLong(5));
        assertEquals( Long.valueOf(5), DataConverter.toLong((long)5));
        assertEquals( Long.valueOf(5), DataConverter.toLong((float)5));
        assertEquals( Long.valueOf(5), DataConverter.toLong((double)5));
        //assertEquals( Double.valueOf(Double.MAX_VALUE), Double.valueOf( (double) DataConverter.toLong(Double.MAX_VALUE)));
        assertEquals( Float.valueOf((float)Long.MIN_VALUE+1f), Float.valueOf( (float) DataConverter.toLong(Float.valueOf((float)Long.MIN_VALUE+1f))));
        assertEquals( Float.valueOf((float)Long.MIN_VALUE-1f), Float.valueOf( (float) DataConverter.toLong(Float.valueOf((float)Long.MIN_VALUE-1f))));
        assertEquals( Double.valueOf((double)Long.MIN_VALUE-1d), Double.valueOf( (double) DataConverter.toLong(Double.valueOf((double)Long.MIN_VALUE-1d))));
        assertEquals( Long.valueOf(Long.MAX_VALUE-100L), DataConverter.toLong(new Date(Long.MAX_VALUE-100)));
        assertEquals( Long.valueOf(5), DataConverter.toLong("5"));
        assertNull( DataConverter.toLong(""));
    }

    @Test
    public void testLongFails() {

        try {
            DataConverter.toLong(Double.MAX_VALUE);
            fail( "Max value should fail" );
        } catch( NumberFormatException e3 ) {
            try {
                DataConverter.toLong("fail");
                fail("Convert should fail");
            } catch (NumberFormatException e5) {
                try {
                    DataConverter.toLong(new LoaderOptions());
                    fail("Convert should fail");
                } catch (Exception e6) {
                    assertTrue(e6 instanceof NumberFormatException);
                }
            }
        }
    }


    @Test
    public void testFloatConvert() {
        assertEquals( Float.valueOf(5), DataConverter.toFloat((byte)5));
        assertEquals( Float.valueOf(5), DataConverter.toFloat((short)5));
        assertEquals( Float.valueOf(5), DataConverter.toFloat(5));
        assertEquals( Float.valueOf(Long.MAX_VALUE), DataConverter.toFloat(Long.MAX_VALUE));
        assertEquals( Float.valueOf(5), DataConverter.toFloat((float)5));
        assertEquals( Float.valueOf(5), DataConverter.toFloat((double)5));
        assertEquals( Double.valueOf(-5.5d), Double.valueOf( DataConverter.toFloat((double)-5.5d)));
        assertEquals( Float.valueOf(Long.MAX_VALUE-100L), Float.valueOf( (float) DataConverter.toFloat(new Date(Long.MAX_VALUE-100L))));
        assertEquals( Float.valueOf(Float.MAX_VALUE-100f), Float.valueOf( (float) DataConverter.toFloat( Float.MAX_VALUE-100f)));
        assertEquals( Float.valueOf(5), DataConverter.toFloat("5"));
        assertEquals( Float.valueOf(5.5f), DataConverter.toFloat("5.5"));
        assertNull( DataConverter.toInt(""));
    }

    @Test
    public void testFloatFails() {

        try {
            DataConverter.toFloat( "fail");
            fail( "Convert should fail" );
        } catch( NumberFormatException e5 ) {
            try {
                DataConverter.toFloat( new LoaderOptions());
                fail( "Convert should fail" );
            } catch( Exception e6 ) {
                assertTrue( e6 instanceof NumberFormatException );
            }
        }
    }

    @Test
    public void testDateConvert() {
        assertEquals( new Date(5), DataConverter.toDate((byte)5));
        assertEquals( new Date(5), DataConverter.toDate((short)5));
        assertEquals( new Date(5), DataConverter.toDate(5));
        assertEquals( new Date(5), DataConverter.toDate((long)5));
        assertEquals( new Date(5), DataConverter.toDate((float)5));
        assertEquals( new Date(5), DataConverter.toDate((double)5));
        assertEquals( new Date(Long.MAX_VALUE-100L), DataConverter.toDate(new Date(Long.MAX_VALUE-100)));
        assertEquals( new Date(23412341234L), DataConverter.toDate("23412341234"));
        assertNull( DataConverter.toLong(""));
    }

    @Test
    public void testDateFails() {

        try {
            DataConverter.toDate(Double.MAX_VALUE);
            fail( "Max value should fail" );
        } catch( NumberFormatException e3 ) {
            try {
                DataConverter.toDate("fail");
                fail("Convert should fail");
            } catch (NumberFormatException e5) {
                try {
                    DataConverter.toDate(new LoaderOptions());
                    fail("Convert should fail");
                } catch (Exception e6) {
                    assertTrue(e6 instanceof NumberFormatException);
                }
            }
        }
    }


    @Test
    public void testStringConvert() {
        assertEquals( "5", DataConverter.toString((byte)5));
        assertEquals( "5", DataConverter.toString((short)5));
        assertEquals( "5", DataConverter.toString(5));
        assertEquals( "5", DataConverter.toString((long)5));
        assertEquals( ""+Long.MAX_VALUE, DataConverter.toString(Long.MAX_VALUE));
        assertEquals( "5.0", DataConverter.toString((float)5));
        assertEquals( ""+Float.MAX_VALUE, DataConverter.toString(Float.MAX_VALUE));
        assertEquals( "5.0", DataConverter.toString((double)5));
        assertEquals( ""+Double.MAX_VALUE, DataConverter.toString(Double.MAX_VALUE));
        assertEquals( ""+(Long.MAX_VALUE-100), DataConverter.toString(new Date(Long.MAX_VALUE-100)));
        assertEquals( "5", DataConverter.toString("5"));
        assertEquals( "", DataConverter.toString(""));
        LoaderOptions o = new LoaderOptions();
        assertEquals( ""+o, DataConverter.toString(o));
        assertNull( DataConverter.toString(null));
    }

    // TODO: Add ArrayList tests
}
