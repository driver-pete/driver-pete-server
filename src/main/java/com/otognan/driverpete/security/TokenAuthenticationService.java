package com.otognan.driverpete.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationService {

	private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	private static final String AUTH_COOKIE_NAME = "AUTH-TOKEN";
	private static final long TEN_DAYS = 1000 * 60 * 60 * 24 * 10;

	private final TokenHandler tokenHandler;

	@Autowired
	public TokenAuthenticationService(@Value("${token.secret}") String secret) {
		tokenHandler = new TokenHandler(DatatypeConverter.parseBase64Binary(secret));
	}

	public void addAuthenticatedUser(HttpServletResponse response, User user) {
		user.setExpires(System.currentTimeMillis() + TEN_DAYS);
		final String token = tokenHandler.createTokenForUser(user);

		// Put the token into a cookie because the client can't capture response
		// headers of redirects / full page reloads.
		// (Its reloaded as a result of this response triggering a redirect back to "/")
	    Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, token);
	    authCookie.setPath("/");
		response.addCookie(authCookie);
	}

	public User getAuthenticatedUser(HttpServletRequest request) {
		// to prevent CSRF attacks we still only allow authentication using a custom HTTP header
		// (it is up to the client to read our previously set cookie and put it in the header)
		final String token = request.getHeader(AUTH_HEADER_NAME);
		if (token != null) {
			return tokenHandler.parseUserFromToken(token);
		}
		return null;
	}
}
