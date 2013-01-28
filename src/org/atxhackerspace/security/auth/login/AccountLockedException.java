package org.atxhackerspace.security.auth.login;

import javax.security.auth.login.LoginException;

public class AccountLockedException extends LoginException {
	public AccountLockedException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4826000984657831566L;

}
