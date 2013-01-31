/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.connector.jdbc;

import org.apache.sqoop.connector.jdbc.configuration.ConnectionConfiguration;
import org.apache.sqoop.connector.jdbc.configuration.ExportJobConfiguration;
import org.apache.sqoop.connector.jdbc.configuration.ImportJobConfiguration;
import org.apache.sqoop.model.MJob;
import org.apache.sqoop.validation.Status;
import org.apache.sqoop.validation.Validation;
import org.apache.sqoop.validation.Validator;

/**
 * Validator to ensure that user is supplying valid input
 */
public class GenericJdbcValidator extends Validator {

  @Override
  public Validation validateConnection(Object configuration) {
    Validation validation = new Validation(ConnectionConfiguration.class);
    ConnectionConfiguration config = (ConnectionConfiguration)configuration;

    if(config.connection.jdbcDriver == null) {
      validation.addMessage(Status.UNACCEPTABLE, "connection", "jdbcDriver", "Driver can't be empty");
    } else {
      try {
        Class.forName(config.connection.jdbcDriver);
      } catch (ClassNotFoundException e) {
        validation.addMessage(Status.UNACCEPTABLE, "connection", "jdbcDriver", "Can't load specified driver");
      }
    }

    if(config.connection.connectionString == null) {
      validation.addMessage(Status.UNACCEPTABLE, "connection", "connectionString", "JDBC URL can't be empty");
    } else if(!config.connection.connectionString.startsWith("jdbc:")) {
      validation.addMessage(Status.UNACCEPTABLE, "connection", "connectionString", "This do not seem as a valid JDBC URL");
    }

    // TODO: Try to connect to database when form level validations will be supported

    return validation;
  }

  @Override
  public Validation validateJob(MJob.Type type, Object jobConfiguration) {
    switch(type) {
      case IMPORT:
        return validateImportJob(jobConfiguration);
      case EXPORT:
        return validateExportJob(jobConfiguration);
      default:
        return super.validateJob(type, jobConfiguration);
    }
  }

  private Validation validateExportJob(Object jobConfiguration) {
    Validation validation = new Validation(ExportJobConfiguration.class);
    ExportJobConfiguration configuration = (ExportJobConfiguration)jobConfiguration;

    // TODO: Move those message to form level when it will be supported
    if(configuration.table.tableName == null && configuration.table.sql == null) {
      validation.addMessage(Status.UNACCEPTABLE, "table", "tableName", "Either table name or SQL must be specified");
      validation.addMessage(Status.UNACCEPTABLE, "table", "sql", "Either table name or SQL must be specified");
    }
    if(configuration.table.tableName != null && configuration.table.sql != null) {
      validation.addMessage(Status.UNACCEPTABLE, "table", "tableName", "Both table name and SQL cannot be specified");
      validation.addMessage(Status.UNACCEPTABLE, "table", "sql", "Both table name and SQL cannot be specified");
    }

    return validation;
  }

  private Validation validateImportJob(Object jobConfiguration) {
    Validation validation = new Validation(ImportJobConfiguration.class);
    ImportJobConfiguration configuration = (ImportJobConfiguration)jobConfiguration;

    // TODO: Move those message to form level when it will be supported
    if(configuration.table.tableName == null && configuration.table.sql == null) {
      validation.addMessage(Status.UNACCEPTABLE, "table", "tableName", "Either table name or SQL must be specified");
      validation.addMessage(Status.UNACCEPTABLE, "table", "sql", "Either table name or SQL must be specified");
    }
    if(configuration.table.tableName != null && configuration.table.sql != null) {
      validation.addMessage(Status.UNACCEPTABLE, "table", "tableName", "Both table name and SQL cannot be specified");
      validation.addMessage(Status.UNACCEPTABLE, "table", "sql", "Both table name and SQL cannot be specified");
    }

    return validation;
  }
}
