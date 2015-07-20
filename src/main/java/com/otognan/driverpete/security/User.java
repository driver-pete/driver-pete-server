package com.otognan.driverpete.security;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.social.security.SocialUserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }) })
public class User implements SocialUserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	@JsonIgnore
	private String providerId;

	@NotNull
	@JsonIgnore
	private String providerUserId;

	@NotNull
	@JsonIgnore
	private String accessToken;

	@NotNull
	@Size(min = 4, max = 30)
	private String username;

	@Transient
	private long expires;

	@NotNull
	private boolean accountExpired;

	@NotNull
	private boolean accountLocked;

	@NotNull
	private boolean credentialsExpired;

	@NotNull
	private boolean accountEnabled;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> roles = new HashSet<String>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@JsonIgnore
	public String getUserId() {
		return id.toString();
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	@JsonIgnore
	public Set<GrantedAuthority> getAuthorities() {
	    Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
	    for (String role : this.roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
		return authorities;
	}

	// Use Roles as external API
	public Set<String> getRoles() {
	    return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void grantRole(String role) {
        if (!role.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Role has to start with ROLE_ prefix");
        }
        this.roles.add(role);
    }

	public void revokeRole(String role) {
	    this.roles.remove(role);
	}

	public boolean hasRole(String role) {
	    return this.roles.contains(role);
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return !accountExpired;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return !accountEnabled;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getUsername();
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		throw new IllegalStateException("password should never be used");
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
