package com.galapea.techblog.ott.security;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOneTimeTokenService implements OneTimeTokenService {
  private final Map<String, OneTimeToken> oneTimeTokens = new ConcurrentHashMap<>();

  private Clock clock = Clock.systemUTC();

  @Override
  @NonNull
  public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
    String token = UUID.randomUUID().toString();
    Instant expiresAt = this.clock.instant().plus(5, ChronoUnit.MINUTES);

    OneTimeToken oneTimeToken = new DefaultOneTimeToken(token, request.getUsername(), expiresAt);
    oneTimeTokens.put(token, oneTimeToken);

    return oneTimeToken;
  }

  @Override
  @Nullable
  public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
    log.info("Consume token: {}", authenticationToken.getTokenValue());
    OneTimeToken oneTimeToken = oneTimeTokens.remove(authenticationToken.getTokenValue());
    if (oneTimeToken == null || isExpired(oneTimeToken)) {
      return null;
    }
    return oneTimeToken;
  }

  private boolean isExpired(OneTimeToken oneTimeToken) {
    return this.clock.instant().isAfter(oneTimeToken.getExpiresAt());
  }
}
