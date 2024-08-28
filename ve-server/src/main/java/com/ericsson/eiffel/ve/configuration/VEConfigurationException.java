package com.ericsson.eiffel.ve.configuration;

import com.ericsson.duraci.exceptions.EiffelException;

public class VEConfigurationException extends EiffelException {

	private static final long serialVersionUID = 8647619932505059559L;

	public VEConfigurationException(final String msg) {
		super(msg);
	}

	public VEConfigurationException(final String msg, final Throwable e) { 
		super(msg, e);
	}
}
