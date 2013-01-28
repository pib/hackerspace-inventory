package org.atxhackerspace.security.auth.login;

import javax.security.auth.login.LoginException;

public class CredentialException extends LoginException {

	public CredentialException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8234835731493753582L;

}
