package com.otognan.driverpete.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/*
 * As described here,
 * http://stackoverflow.com/questions/9815756/injection-of-autowired-dependencies-failed-while-using-transactional
 * Transactional leads to creation of proxies so that you can not autowire the UserService
 * directly, you have to use an interface (SocialUserService). The solution is to use
 * proxy-target-class="true" in xml. I found that you can achieve the same with 
 * @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS) annotation.
 */
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SocialUserService implements SocialUserDetailsService, UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    /*
     * from SocialUserDetailsService: load SocialUserDetails by user id.
     * User implements SocialUserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUserId(String userId)  {
        final User user = userRepo.findById(Long.valueOf(userId));
        return checkUser(user);
    }

    /*
     * from UserDetailsService: load UserDetails by username.
     * User implements SocialUserDetails which in turn implements UserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) {
        final User user = userRepo.findByUsername(username);
        return checkUser(user);
    }

    /*
     * This function used in UsersConnectionRepository in order to find user from its Facebook connection
     * after user logs in in Facebook
     */
    @Transactional(readOnly = true)
    public User loadUserByProviderIdAndProviderUserId(String providerId, String providerUserId) {
        final User user = userRepo.findByProviderIdAndProviderUserId(providerId, providerUserId);
        return checkUser(user);
    }

    /*
     * This function is used in UsersConnectionRepository to update user's access token from
     * Facebook connection information.
     */
    public void updateUserDetails(User user) {
        userRepo.save(user);
    }

    private User checkUser(User user) {
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        /*
         * This object throws standard exceptions if user account is locked, expired etc.
         * Exception messages might be obtained from a message source.
         */
        AccountStatusUserDetailsChecker checker = new AccountStatusUserDetailsChecker();
        checker.check(user);
        return user;
    }
}
