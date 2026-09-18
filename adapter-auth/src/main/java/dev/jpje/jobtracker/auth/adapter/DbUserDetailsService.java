package dev.jpje.jobtracker.auth.adapter;

import dev.jpje.jobtracker.auth.userdetails.AuthUserDetails;
import dev.jpje.jobtracker.domain.port.outbound.LoadUserPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {
  private final LoadUserPort loadUserPort;

  public DbUserDetailsService(final LoadUserPort loadUserPort) {
    this.loadUserPort = loadUserPort;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    return loadUserPort.findByUsername(username)
      .map(AuthUserDetails::new)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
