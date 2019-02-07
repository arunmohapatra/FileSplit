//**************************************************************
//  Copyright (c) 2001-2014 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import java.io.IOException;
import java.net.Socket;

public class Port {
	public static final int	MAX_PORT_VALUE	= 65535;
	public static final int	MIN_PORT_VALUE	= 1;
	private int				portNumber		= 0;
	private String			portName;

	public Port(int portNumber) throws PortRangeException, ServiceNameException {
		this(String.valueOf(portNumber));
	}

	public Port(String portName) throws PortRangeException, ServiceNameException {
		if (portName != null) {
			setName(portName.trim());
		} else {
			setName(null);
		}
	}

	/**
	 * This calls setPortNum which sets-up the rest of the port values.
	 */
	public void setName(String portName) throws PortRangeException, ServiceNameException {
		this.portName = portName;
		setPortNum(portName);
	}

	/**
	 * This method returns a string for the name originally passed in.
	 */
	public String getName() {
		return portName;
	}

	/**
	 * Returns 0 if the port is invalid else returns the integer value of the
	 * resolved portName
	 */
	public int getPortInt() {
		return portNumber;
	}

	/**
	 * Returns true if the port is in use Returnse false if port is not in use
	 */
	public boolean isInUse() {
		boolean portInUse = false;
		try {
			Socket s = new Socket("localhost", portNumber);
			if (s != null) {
				portInUse = true;
				s.close();
			}
		} catch (IOException excp) {
			portInUse = false;
		}

		return portInUse;
	}

	private void validatePortRange(int port) throws PortRangeException {
		if (port < MIN_PORT_VALUE || port > MAX_PORT_VALUE)
			throw new PortRangeException(port);
	}

	private void setPortNum(String portNumber) throws PortRangeException, ServiceNameException {

		try {
			this.portNumber = Integer.parseInt(portNumber);
			validatePortRange(this.portNumber);
		} catch (NumberFormatException Excp) {
			if (portNumber != null && NetLib.getPortNumByName(portNumber, "tcp") > 0) {
				this.portNumber = NetLib.getPortNumByName(portNumber, "tcp");
				validatePortRange(this.portNumber);
			} else {
				if (portNumber == null)
					portNumber = "null";
				throw new ServiceNameException(portNumber);
			}
		}
	}

	// -- inner classes follow
	public static class PortException extends Exception {
		PortException(String message) {
			super(message);
		}
	}

	public static class PortRangeException extends PortException {
		private static String	errMessage	= "Specify a value greater than " + MIN_PORT_VALUE
													+ " and less than or equal to " + MAX_PORT_VALUE + ": ";

		PortRangeException(int port) {
			super(errMessage + port);
		}
	}

	public static class ServiceNameException extends PortException {
		private static String	message	= "An invalid service name was specified: ";

		ServiceNameException(String port) {
			super(message + port);
		}
	}
}
