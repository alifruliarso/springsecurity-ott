package com.galapea.techblog.ott.security;

import com.galapea.techblog.ott.model.UserRecord;
import com.galapea.techblog.ott.service.UsersContainerClient;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

  private final UsersContainerClient usersContainerClient;

  @Override
  public UserDetails loadUserByUsername(String email) {
    UserRecord user = usersContainerClient.getUserByEmail(email);
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }

    List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    if ("admin@example.com".equals(user.email())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    return new org.springframework.security.core.userdetails.User(
        user.email(), user.password(), authorities);
  }
}
