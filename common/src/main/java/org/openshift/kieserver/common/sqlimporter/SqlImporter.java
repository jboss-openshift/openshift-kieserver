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

package org.openshift.kieserver.common.sqlimporter;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.logging.Logger;

/**
 * Created by fspolti on 6/3/16.
 */

@Singleton
@Startup
public class SqlImporter {

    private final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    private final String DEFAULT_COMMAND_DELIMITER = ";";
    private final String QUARTZ_JNDI = System.getenv("QUARTZ_JNDI");
    private Logger log = Logger.getLogger(SqlImporter.class.getName());
    private DataSource ds;
    private Context ctx;
    private String DB_TYPE = null;
    private String DB_SERVICE_PREFIX = System.getenv("DB_SERVICE_PREFIX_MAPPING");
    private String SQL_SCRIPT = System.getProperty("jboss.home.dir") + "/bin";

    @PostConstruct
    public void importSqlFile() throws SQLException {

        if ("".equals(QUARTZ_JNDI) || null == QUARTZ_JNDI) {
            log.info("QUARTZ_JNDI env not found, skipping SqlImporter");
        } else {
            getDatabaseType();
            if (null != DB_TYPE) {
                log.info("Starting SqlImporter...");
                doImport(SQL_SCRIPT);
            }
        }
    }

    /*
    * Prepare the needed variables and then call the scriptImporter to start the import task.
    * @throws SQLException for SQL related issues and Exception for any other issue
    */
    private void doImport(String script) throws SQLException {

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
            log.severe("Failed to import the script " + script + ", error message: " + e.getMessage());

        } finally {
            conn.close();
        }
    }

    /*
    * Import the script
    * @throws SQLException for SQL related issues and Exception for any other issue
    */
    private void scriptImporter(Connection conn) throws IOException, SQLException {

        log.info("Reading SQL file: " + SQL_SCRIPT);
        Statement stm = conn.createStatement();
        Reader reader = new BufferedReader(new FileReader(SQL_SCRIPT));
        StringBuffer command = new StringBuffer();

        try {

            BufferedReader lineReader = new BufferedReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null) {

                String trimmedCommand = line.trim();

                if (isComment(trimmedCommand)) {
                    log.info(trimmedCommand);
                } else if (commandIsReady(trimmedCommand)) {
                    command.append(line.substring(0, line.lastIndexOf(DEFAULT_COMMAND_DELIMITER)));
                    command.append(LINE_SEPARATOR);
                    log.info("command to execute: \n" + command);

                    //execute sql
                    stm.execute(String.valueOf(command+";"));
                    conn.commit();

                    //clear the previous command
                    command.setLength(0);
                } else if (trimmedCommand.length() > 0) {
                    command.append(line);
                    command.append(LINE_SEPARATOR);
                }
            }

        } catch (Exception e) {
            log.severe("Error during import script execution. Error message: " + e.getMessage());
            conn.rollback();
        } finally {
            stm.close();
        }
    }

    /*
    * Verifies if the given line from script is a comment or not
    * @returns true if the line is a comment and false if is nit a comment.
    */
    private boolean isComment(String trimmedLine) {
        return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
    }

    /*
    * Verifies if the entire command line is ready to be executed.
    * @returns true when the COMMAND_DELIMITER is found or false if the delimiter is not found.
    */
    private boolean commandIsReady(String trimmedCommand) {
        return trimmedCommand.endsWith(DEFAULT_COMMAND_DELIMITER) || trimmedCommand.equals(DEFAULT_COMMAND_DELIMITER);
    }


    /*
    * Verifies if the Quartz Tables already exists
    * @returns true or false
    * @params SqlConnection
    * @throws SQLException for SQL related issues and Exception for any other issue
    */
    public boolean tablesExists(Connection conn) throws SQLException {

        DatabaseMetaData md = conn.getMetaData();
        ResultSet table = md.getTables(null, null, DB_TYPE.equals("MYSQL") ? "QRTZ_JOB_DETAILS" : "qrtz_job_details", null);

        if (table.next()) {
            return true;
        }
        return false;

    }

    /*
    * Returns the SQL connection
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

    /*
    * Returns the database type
    * MYSQL or POSTGRESQL
    */
    private void getDatabaseType() {

        if (null != DB_SERVICE_PREFIX && DB_SERVICE_PREFIX.toUpperCase().contains("MYSQL")) {
            DB_TYPE = "MYSQL";
            SQL_SCRIPT += "/quartz_tables_mysql.sql";
        } else if (null != DB_SERVICE_PREFIX && DB_SERVICE_PREFIX.toUpperCase().contains("POSTGRESQL")) {
            DB_TYPE = "POSTGRESQL";
            SQL_SCRIPT += "/quartz_tables_postgres.sql";
        }
    }
}