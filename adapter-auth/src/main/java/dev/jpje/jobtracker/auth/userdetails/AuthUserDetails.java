package dev.jpje.jobtracker.auth.userdetails;

import java.util.Collection;
import java.util.List;

import dev.jpje.jobtracker.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthUserDetails implements UserDetails {
  private final transient User user;

  public AuthUserDetails(final User user) {
    this.user = user;
  }

  public User user() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
  }

  @Override
  public String getPassword() {
    return user.passwordHash();
  }

  @Override
  public String getUsername() {
    return user.username().value();
  }

}
