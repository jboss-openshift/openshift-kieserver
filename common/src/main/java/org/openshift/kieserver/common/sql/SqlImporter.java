/**
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.openshift.kieserver.common.sql;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.logging.Logger;

/**
 * @author fspolti
 */

@Singleton
@Startup
public class SqlImporter {

    private final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    private final String DEFAULT_COMMAND_DELIMITER = ";";
    private final String QUARTZ_JNDI = System.getenv("QUARTZ_JNDI");
    // use the DEFAULT if none is passed, it will be overridden on getDatabaseType, if needed.
    private String CUSTOM_COMMAND_DELIMITER = DEFAULT_COMMAND_DELIMITER;
    private DataSource ds;
    private Context ctx;
    private String DB_TYPE = null;
    private String SQL_SCRIPT = System.getProperty("jboss.home.dir") + "/bin/";

    @PostConstruct
    public void importSqlFile() throws SQLException {

        if ("".equals(QUARTZ_JNDI) || null == QUARTZ_JNDI) {
            log.info("QUARTZ_JNDI env not found, skipping SqlImporter");
        } else {
            getDatabaseType();
            if (null != DB_TYPE) {
                log.info("Starting SqlImporter...");
                doImport();
            }
        }
    }

    /**
     * Prepare the needed variables and then call the scriptImporter to start the import task.
     *
     * @throws SQLException for SQL related issues and Exception for any other issue
     */
    private void doImport() throws SQLException {

        Connection conn = getConnection();

        try {

            if (tablesExists(conn)) {
                log.info("Tables already imported, skipping...");
            } else {
                conn.setAutoCommit(false);
                scriptImporter(conn);
                log.info("Quartz tables successfully imported from " + SQL_SCRIPT);
            }

        } catch (Exception e) {
            log.severe("Failed to import the script " + SQL_SCRIPT + ", error message: " + e.getMessage());

        } finally {
            conn.close();
        }
    }

    /**
     * Import the script
     *
     * @throws SQLException for SQL related issues and Exception for any other issue
     */
    private void scriptImporter(Connection conn) throws IOException, SQLException {

        if (new File(SQL_SCRIPT).exists()) {
            log.info("Reading SQL file: " + SQL_SCRIPT);
            Statement stm = conn.createStatement();
            BufferedReader reader = new BufferedReader(new FileReader(SQL_SCRIPT));
            StringBuffer command = new StringBuffer();

            try {

                String line;
                while ((line = reader.readLine()) != null) {

                    String trimmedCommand = line.trim();

                    if (isComment(trimmedCommand)) {
                        log.info(trimmedCommand);
                    } else if (commandIsReady(trimmedCommand)) {
                        try {
                            command.append(line.substring(0, line.lastIndexOf(CUSTOM_COMMAND_DELIMITER)));
                        } catch (StringIndexOutOfBoundsException e ) {
                            // In SQL SERVER quartz script, there are two delimiters, GO and the default ";", if the first fails, rely on the DEFAULT DELIMITER
                            command.append(line.substring(0, line.lastIndexOf(DEFAULT_COMMAND_DELIMITER)));
                        }
                        command.append(LINE_SEPARATOR);
                        log.info("command to execute: \n" + command);

                        //execute sql
                        stm.execute(String.valueOf(command));
                        conn.commit();

                        //clear the previous command
                        command.setLength(0);
                    } else if (trimmedCommand.length() > 0) {
                        command.append(line);
                        command.append(LINE_SEPARATOR);
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw new SQLException("Error during import script execution. Error message: " + e.getMessage());
            } finally {
                reader.close();
                stm.close();
            }
        } else {
            log.warning("File " + SQL_SCRIPT + " Not found, aborting.");
        }
    }

    /**
     * Verifies if the given line from script is a comment or not
     *
     * @returns true if the line is a comment and false if is nit a comment.
     */
    private boolean isComment(String trimmedLine) {
        return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
    }

    /**
     * Verifies if the entire command line is ready to be executed.
     *
     * @returns true when the COMMAND_DELIMITER is found or false if the delimiter is not found.
     */
    private boolean commandIsReady(String trimmedCommand) {
        return (trimmedCommand.endsWith(DEFAULT_COMMAND_DELIMITER) || trimmedCommand.equals(DEFAULT_COMMAND_DELIMITER)) ||
                (trimmedCommand.endsWith(CUSTOM_COMMAND_DELIMITER) || trimmedCommand.equals(CUSTOM_COMMAND_DELIMITER));
    }


    /**
     * Verifies if the Quartz Tables already exists
     *
     * @throws SQLException for SQL related issues and Exception for any other issue
     * @returns true or false
     * @params SqlConnection
     */
    public boolean tablesExists(Connection conn) throws SQLException {

        String tableName = DB_TYPE.equals("POSTGRESQL") ? "qrtz_job_details" : "QRTZ_JOB_DETAILS";

        DatabaseMetaData md = conn.getMetaData();
        ResultSet table = md.getTables(null, null, tableName, null);

        if (table.next()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the SQL connection
     *
     * @throws SQLException for SQL related issues and Exception for any other issue
     */
    private Connection getConnection() throws SQLException {
        try {
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(QUARTZ_JNDI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds.getConnection();
    }

    /**
     * Sets the database type and defines the quartz sql file name.
     * The Database type will be exported by the kieserver-setup.sh
     */
    private void getDatabaseType() {

        DB_TYPE = System.getProperty("org.openshift.kieserver.common.sql.dbtype").toUpperCase();

        if (null == DB_TYPE || DB_TYPE.isEmpty()) {
            log.warning("Property org.openshift.kieserver.common.sql.dbtype not set, sqlImporter will not run properly");
        } else {
            switch (DB_TYPE) {
                case "MYSQL":
                    SQL_SCRIPT += "quartz_tables_mysql.sql";
                    break;
                case "POSTGRESQL":
                    SQL_SCRIPT += "quartz_tables_postgres.sql";
                    break;
                case "ORACLE":
                    SQL_SCRIPT += "quartz_tables_oracle.sql";
                    break;
                case "SQLSERVER":
                    SQL_SCRIPT += "quartz_tables_sqlserver.sql";
                    CUSTOM_COMMAND_DELIMITER="GO";
                    break;
                case "DB2":
                    SQL_SCRIPT += "quartz_tables_db2.sql";
                    break;
            }
        }
    }
}