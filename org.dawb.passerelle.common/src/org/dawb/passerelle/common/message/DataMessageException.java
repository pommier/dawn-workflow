/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.passerelle.common.message;

import java.util.Collection;

import javax.management.MBeanServerConnection;

import org.dawb.passerelle.common.Activator;
import org.dawb.passerelle.common.actors.ActorUtils;
import org.dawb.workbench.jmx.RemoteWorkbenchAgent;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException.Severity;

public class DataMessageException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7005072679084823511L;
	private DataMessageComponent dataMessageComponent;

	public DataMessageException(String    message, 
								Object    context,
								Throwable rootException) {
		this(message, context, new DataMessageComponent(), rootException);
	}
	public DataMessageException(String    message, 
								Object    context,
								Collection<DataMessageComponent> sets,
								Throwable rootException) {
		this(message, context, MessageUtils.mergeAll(sets), rootException);
	}
	public DataMessageException(String    message, 
			                    Object    context,
			                    DataMessageComponent comp,
			                    Throwable rootException) {
		
		super(Severity.NON_FATAL, message, context, rootException);
		
		this.dataMessageComponent = comp;
		dataMessageComponent.putScalar("message_text", message);
		dataMessageComponent.setError(true);
		if (rootException!=null) dataMessageComponent.putScalar("exception_text", rootException.getMessage()!=null?rootException.getMessage():rootException.getClass().getName());
		
		try {	// Send this to the workbench log so that it is visible in the log view.		
			final MBeanServerConnection client = RemoteWorkbenchAgent.getServerConnection(1000);
			client.invoke(RemoteWorkbenchAgent.REMOTE_WORKBENCH, "logStatus", new Object[]{Activator.getDefault().getBundle().getSymbolicName(), message, rootException}, new String[]{String.class.getName(),String.class.getName(),Throwable.class.getName()});		
		} catch (Throwable ignored) {
			// Nevermind then!
		}

	}

	public DataMessageComponent getDataMessageComponent() {
		return dataMessageComponent;
	}

	public void setDataMessageComponent(DataMessageComponent dataMessageComponent) {
		this.dataMessageComponent = dataMessageComponent;
	}

}
