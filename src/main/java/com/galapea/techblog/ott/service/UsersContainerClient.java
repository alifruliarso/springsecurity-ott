package com.galapea.techblog.ott.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.galapea.techblog.ott.exception.GridDbException;
import com.galapea.techblog.ott.model.UserRecord;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class UsersContainerClient {
  private static final String CONTAINER_NAME = "Users";
  private final RestClient restClient;
  private final String baseUrl;

  public UsersContainerClient(
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
                  log.error("GridDB API Response error body: {}", errorBody);
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

  public static class UsersQueryRequest {
    @JsonProperty("offset")
    private int offset;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("sort")
    private String sort;

    // Constructor
    public UsersQueryRequest(int offset, int limit, String condition, String sort) {
      this.offset = offset;
      this.limit = limit;
      this.condition = condition;
      this.sort = sort;
    }

    // Getters and setters
    public int getOffset() {
      return offset;
    }

    public void setOffset(int offset) {
      this.offset = offset;
    }

    public int getLimit() {
      return limit;
    }

    public void setLimit(int limit) {
      this.limit = limit;
    }

    public String getCondition() {
      return condition;
    }

    public void setCondition(String condition) {
      this.condition = condition;
    }

    public String getSort() {
      return sort;
    }

    public void setSort(String sort) {
      this.sort = sort;
    }
  }

  /**
   * Query users from the Users container with specified parameters
   *
   * @param offset Starting position of the results
   * @param limit Maximum number of results to return
   * @param condition Query condition (e.g., "email == 'admin'")
   * @param sort Sort order (e.g., "email asc")
   * @return Query result from GridDB
   */
  public List<UserRecord> queryUsers(int offset, int limit, String condition, String sort) {
    UsersQueryRequest request = new UsersQueryRequest(offset, limit, condition, sort);
    String url = String.format("/containers/%s/rows", CONTAINER_NAME);
    GridDbCloudContainerOutput response = post(url, request, GridDbCloudContainerOutput.class);
    List<UserRecord> userRecords = new ArrayList<>();
    if (response == null || response.total() == 0) {
      return userRecords;
    }
    for (List<String> row : response.rows()) {
      if (row.size() >= 4) {
        userRecords.add(new UserRecord(row.get(0), row.get(1), row.get(2), row.get(3)));
      }
    }
    return userRecords;
  }

  /**
   * Query users with default parameters (first 100 records)
   *
   * @return Query result from GridDB
   */
  public List<UserRecord> queryUsers() {
    return queryUsers(0, 100, null, null);
  }

  private <T> T post(String uri, Object body, Class<T> responseType) {
    try {
      return restClient.post().uri(uri).body(body).retrieve().body(responseType);
    } catch (GridDbException e) {
      throw e;
    } catch (Exception e) {
      throw new GridDbException(
          "Failed to execute POST request", HttpStatusCode.valueOf(500), e.getMessage(), e);
    }
  }

  private void post(String uri, Object body) {
    try {
      restClient.post().uri(uri).body(body).retrieve().toBodilessEntity();
    } catch (GridDbException e) {
      throw e;
    } catch (Exception e) {
      throw new GridDbException(
          "Failed to execute POST request", HttpStatusCode.valueOf(500), e.getMessage(), e);
    }
  }

  public UserRecord getUserById(String userId) {
    String statement =
        String.format("SELECT id, email, name, \"password\" FROM Users where id == '%s'", userId);
    return getOneUser(statement);
  }

  public UserRecord getUserByEmail(String email) {
    String statement =
        String.format("SELECT id, email, name, \"password\" FROM Users where email == '%s'", email);
    return getOneUser(statement);
  }

  private UserRecord getOneUser(String statement) {
    String type = "sql-select";
    GridDbCloudSQLSelectInput input = new GridDbCloudSQLSelectInput(type, statement);
    var response = post("/sql", List.of(input), GridDbCloudSQLOutPut[].class);
    for (GridDbCloudSQLOutPut item : response) {
      log.info("Response: {}", item);
    }
    log.info("Output: {}", response[0]);
    if (response[0].results().size() == 0) {
      return null;
    }
    UserRecord foundUser = null;
    for (List<String> row : response[0].results()) {
      if (row.size() < 4) {
        break;
      }
      foundUser = new UserRecord(row.get(0), row.get(1), row.get(2), row.get(3));
    }
    log.info("Found user: {}", foundUser);
    return foundUser;
  }

  public void insert(UserRecord user) {
    String stmt =
        "INSERT INTO Users(id, email, name, \"password\") VALUES ('"
            + user.id()
            + "', '"
            + user.email()
            + "', '"
            + user.name()
            + "', '"
            + user.password()
            + "')";
    GridDbCloudSQLInsert insert = new GridDbCloudSQLInsert(stmt);

    post("/sql/update", List.of(insert));
  }

  public void update(UserRecord user) {
    String statement =
        String.format("UPDATE Users set name = '%s' where id == '%s'", user.name(), user.id());

    GridDbCloudSQLInsert insert = new GridDbCloudSQLInsert(statement);

    post("/sql/update", List.of(insert));
  }

  public void deleteUser(String id) {
    String url = String.format("/containers/%s/rows", CONTAINER_NAME);
    restClient.method(HttpMethod.DELETE).uri(url).body(List.of(id)).retrieve().toBodilessEntity();
  }
}

record GridDbCloudSQLInsert(@JsonProperty("stmt") String statement) {}

record GridDbCloudSQLSelectInput(String type, @JsonProperty("stmt") String statement) {}

record GDCColumnInfo(@JsonProperty("name") String name, @JsonProperty("type") String type) {}

record GridDbCloudSQLOutPut(
    @JsonProperty("columns") List<GDCColumnInfo> columns,
    @JsonProperty("results") List<List<String>> results,
    @JsonProperty("responseSizeByte") long responseSizeByte) {}

record GridDbCloudContainerOutput(
    @JsonProperty("columns") List<GDCColumnInfo> columns,
    @JsonProperty("rows") List<List<String>> rows,
    @JsonProperty("total") long total,
    @JsonProperty("offset") long offset,
    @JsonProperty("limit") long limit) {}
