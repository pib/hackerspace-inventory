package org.atxhackerspace.security.auth.login;

import javax.security.auth.login.LoginException;

public class FailedLoginException extends LoginException {

	public FailedLoginException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3856048080339045054L;

}
