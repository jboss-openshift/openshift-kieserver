package org.openshift.kieserver.common.sql;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SqlImporterTest {


    @Before
    public void prepareEnv() {
        System.setProperty("jboss.home.dir", "/tmp/jboss");
    }


    @Test
    public void testGetDatabaseTypeWithOracle() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SqlImporter importer = new SqlImporter();
        System.setProperty("org.openshift.kieserver.common.sql.dbtype", "oracle");
        Assert.assertEquals("/tmp/jboss/bin/quartz_tables_oracle.sql", getFieldValue(importer));

        Field field = SqlImporter.class.getDeclaredField("DB_TYPE");
        field.setAccessible(true);
        Assert.assertEquals("ORACLE", field.get(importer));

    }


    @Test
    public void testGetDatabaseTypeWithSqlServer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SqlImporter importer = new SqlImporter();
        System.setProperty("org.openshift.kieserver.common.sql.dbtype", "sqlserver");
        Assert.assertEquals("/tmp/jboss/bin/quartz_tables_sqlserver.sql", getFieldValue(importer));

        Field field = SqlImporter.class.getDeclaredField("DB_TYPE");
        field.setAccessible(true);
        Assert.assertEquals("SQLSERVER", field.get(importer));
    }

    @Test
    public void testGetDatabaseTypeWithMySQL() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SqlImporter importer = new SqlImporter();
        System.setProperty("org.openshift.kieserver.common.sql.dbtype", "mysql");
        Assert.assertEquals("/tmp/jboss/bin/quartz_tables_mysql.sql", getFieldValue(importer));

        Field field = SqlImporter.class.getDeclaredField("DB_TYPE");
        field.setAccessible(true);
        Assert.assertEquals("MYSQL", field.get(importer));
    }

    @Test
    public void testGetDatabaseTypeWithPostgresql() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SqlImporter importer = new SqlImporter();
        System.setProperty("org.openshift.kieserver.common.sql.dbtype", "postgresql");
        Assert.assertEquals("/tmp/jboss/bin/quartz_tables_postgres.sql", getFieldValue(importer));

        Field field = SqlImporter.class.getDeclaredField("DB_TYPE");
        field.setAccessible(true);
        Assert.assertEquals("POSTGRESQL", field.get(importer));
    }

    @Test
    public void testGetDatabaseTypeWithDB2() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SqlImporter importer = new SqlImporter();
        System.setProperty("org.openshift.kieserver.common.sql.dbtype", "db2");
        Assert.assertEquals("/tmp/jboss/bin/quartz_tables_db2.sql", getFieldValue(importer));

        Field field = SqlImporter.class.getDeclaredField("DB_TYPE");
        field.setAccessible(true);
        Assert.assertEquals("DB2", field.get(importer));
    }


    @Test
    public void testIsEmptyOrNullMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SqlImporter importer = new SqlImporter();
        Method method = SqlImporter.class.getDeclaredMethod("isEmptyOrNull", new Class[] { String.class });
        method.setAccessible(true);

        String nulls = null;

        Assert.assertTrue((Boolean) method.invoke(importer, ""));
        Assert.assertTrue((Boolean) method.invoke(importer, " "));
        Assert.assertTrue((Boolean) method.invoke(importer, nulls));
        Assert.assertFalse((Boolean) method.invoke(importer, "postgresql"));
    }

    private String getFieldValue(SqlImporter importer) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Field field = SqlImporter.class.getDeclaredField("SQL_SCRIPT");
        field.setAccessible(true);

        Method method = SqlImporter.class.getDeclaredMethod("getDatabaseType");
        method.setAccessible(true);
        method.invoke(importer, null);

        return (String) field.get(importer);
    }
}