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
package org.apache.sqoop.client.shell;

import jline.ConsoleReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.sqoop.client.core.ClientError;
import org.apache.sqoop.client.core.Constants;
import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.json.ConnectionBean;
import org.apache.sqoop.model.MConnection;
import org.apache.sqoop.model.MPersistableEntity;
import org.apache.sqoop.validation.Status;
import org.codehaus.groovy.tools.shell.IO;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import static org.apache.sqoop.client.utils.FormFiller.*;
import static org.apache.sqoop.client.core.RequestCache.*;

/**
 *
 */
public class CloneConnectionFunction extends SqoopFunction {



  private IO io;



  @SuppressWarnings("static-access")
  public CloneConnectionFunction(IO io) {
    this.io = io;

    this.addOption(OptionBuilder
      .withDescription(getResource().getString(Constants.RES_PROMPT_CONN_ID))
      .withLongOpt(Constants.OPT_XID)
      .hasArg()
      .create(Constants.OPT_XID_CHAR)
    );
  }

  public Object execute(List<String> args) {
    CommandLine line = parseOptions(this, 1, args);
    if (!line.hasOption(Constants.OPT_XID)) {
      io.out.println(getResource().getString(Constants.RES_ARGS_XID_MISSING));
      return null;
    }

    try {
      cloneConnection(line.getOptionValue(Constants.OPT_XID));
    } catch (IOException ex) {
      throw new SqoopException(ClientError.CLIENT_0005, ex);
    }

    return null;
  }

  private void cloneConnection(String connectionId) throws IOException {

    io.out.println(MessageFormat.format(getResource().getString(Constants
        .RES_CLONE_CLONING_CONN), connectionId ));

    ConsoleReader reader = new ConsoleReader();

    ConnectionBean connectionBean = readConnection(connectionId);

    // TODO(jarcec): Check that we have expected data
    MConnection connection = connectionBean.getConnections().get(0);
    ResourceBundle frameworkBundle
      = connectionBean.getFrameworkBundle();
    ResourceBundle connectorBundle
      = connectionBean.getConnectorBundle(connection.getConnectorId());

    // Remove persistent id as we're making a clone
    connection.setPersistenceId(MPersistableEntity.PERSISTANCE_ID_DEFAULT);

    Status status = Status.FINE;

    io.out.println(getResource().getString(Constants
       .RES_PROMPT_UPDATE_CONN_METADATA));

    do {
      // Print error introduction if needed
      if( !status.canProceed() ) {
        errorIntroduction(io);
      }

      // Fill in data from user
      if(!fillConnection(io, reader, connection,
                         connectorBundle, frameworkBundle)) {
        return;
      }

      status = createConnectionApplyValidations(connection);

    } while(!status.canProceed());

    io.out.println(MessageFormat.format(getResource()
        .getString(Constants.RES_CLONE_CONN_SUCCESSFUL),
        status.name(), connection.getPersistenceId()));
  }
}
