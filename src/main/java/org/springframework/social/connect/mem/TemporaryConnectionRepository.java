package org.springframework.social.connect.mem;

import org.springframework.social.connect.ConnectionFactoryLocator;

/**
 * A short-lived (per request) ConnectionRepository for a single user.
 * It is extending a social connect mem package because InMemoryConnectionRepository is private
 * there for some reason.
 */
public class TemporaryConnectionRepository extends InMemoryConnectionRepository {
    public TemporaryConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        super(connectionFactoryLocator);
    }
}
