package com.galapea.techblog.ott.service;

import com.galapea.techblog.ott.exception.GridDbException;
import com.galapea.techblog.ott.model.GridDbColumn;
import com.galapea.techblog.ott.model.GridDbContainerDefinition;
import com.galapea.techblog.ott.model.GridDbSqlStatement;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GridDbClient {

  private final RestClient restClient;
  private final String baseUrl;

  public GridDbClient(
      @Value("${griddb.base-url}") String baseUrl,
      @Value("${griddb.auth-token}") String authToken) {
    this.baseUrl = baseUrl;
    this.restClient =
        RestClient.builder()
            .baseUrl(this.baseUrl)
            .defaultHeader("Authorization", "Basic " + authToken)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler(
                HttpStatusCode::isError,
                (request, response) -> {
                  log.error("GridDB API response failed: {}", response.getStatusText());
                  String errorBody = new String(response.getBody().readAllBytes());
                  log.error("GridDB API Response body: {}", errorBody);
                  throw new GridDbException(
                      "GridDB API request failed", response.getStatusCode(), errorBody);
                })
            .requestInterceptor(
                (request, body, execution) -> {
                  log.info("GridDB API Request: {} {}", request.getMethod(), request.getURI());
                  if (body != null && body.length > 0) {
                    log.info(
                        "GridDB API Request body: {}", new String(body, StandardCharsets.UTF_8));
                  }
                  log.info("GridDB API Request headers: {}", request.getHeaders());
                  return execution.execute(request, body);
                })
            .build();
  }

  public void executeDdlStatements(List<GridDbSqlStatement> statements) {
    try {
      restClient.post().uri("/sql/ddl").body(statements).retrieve().toBodilessEntity();
    } catch (GridDbException e) {
      throw e;
    } catch (Exception e) {
      throw new GridDbException(
          "Failed to execute DDL statements", HttpStatusCode.valueOf(500), e.getMessage(), e);
    }
  }

  public void createContainer(GridDbContainerDefinition containerDefinition) {
    try {
      restClient.post().uri("/containers").body(containerDefinition).retrieve().toBodilessEntity();
    } catch (GridDbException e) {
      if (e.getStatusCode().value() == 409) {
        return;
      }
      throw e;
    } catch (Exception e) {
      throw new GridDbException(
          "Failed to create container", HttpStatusCode.valueOf(500), e.getMessage(), e);
    }
  }

  public void createTableUser() {
    List<GridDbColumn> columns =
        List.of(
            new GridDbColumn("id", "STRING", Set.of("TREE")),
            new GridDbColumn("email", "STRING"),
            new GridDbColumn("name", "STRING"),
            new GridDbColumn("password", "STRING"));

    GridDbContainerDefinition containerDefinition =
        GridDbContainerDefinition.createContainer("Users", columns);
    createContainer(containerDefinition);
  }

  public void createTableUserSQL() {
    List<GridDbSqlStatement> statements =
        List.of(
            new GridDbSqlStatement(
                "CREATE TABLE IF NOT EXISTS Users ("
                    + "id STRING PRIMARY KEY, "
                    + "email STRING, "
                    + "name STRING, "
                    + "password STRING) "),
            new GridDbSqlStatement("CREATE INDEX IF NOT EXISTS idx_users_email ON Users (email)"));

    executeDdlStatements(statements);
  }
}
