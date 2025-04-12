package com.galapea.techblog.ott.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SendLinkOneTimeTokenGenerationSuccessHandler
    implements OneTimeTokenGenerationSuccessHandler {

  private final OttEmailService emailService;
  private final OneTimeTokenGenerationSuccessHandler redirectHandler =
      new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");
  private final FlashMapManager flashMapManager = new SessionFlashMapManager();

  public SendLinkOneTimeTokenGenerationSuccessHandler(OttEmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  @SneakyThrows
  public void handle(
      HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
      throws IOException, ServletException {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
            .replacePath(request.getContextPath())
            .replaceQuery(null)
            .fragment(null)
            .path("/ott/submit")
            .queryParam("token", oneTimeToken.getTokenValue());
    String link = builder.toUriString();
    CompletableFuture.runAsync(() -> emailService.sendEmail(oneTimeToken.getUsername(), link));
    // redirectHandler.handle(request, response, oneTimeToken);
    RedirectView redirectView = new RedirectView("/ott/sent");
    redirectView.setExposeModelAttributes(false);
    FlashMap flashMap = new FlashMap();
    flashMap.put("token", oneTimeToken.getTokenValue());
    flashMap.put("ottSubmitUrl", link);
    flashMapManager.saveOutputFlashMap(flashMap, request, response);
    redirectView.render(flashMap, request, response);
  }
}
