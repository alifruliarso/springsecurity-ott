package com.galapea.techblog.ott.seeder;

import com.galapea.techblog.ott.model.UserRecord;
import com.galapea.techblog.ott.service.GridDbClient;
import com.galapea.techblog.ott.service.UsersContainerClient;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserSeeder implements CommandLineRunner {

  private final PasswordEncoder passwordEncoder;
  private final GridDbClient gridDbClient;
  private final UsersContainerClient usersContainerClient;

  public UserSeeder(
      PasswordEncoder passwordEncoder,
      GridDbClient gridDbClient,
      UsersContainerClient usersContainerClient) {
    this.passwordEncoder = passwordEncoder;
    this.gridDbClient = gridDbClient;
    this.usersContainerClient = usersContainerClient;
  }

  @Override
  public void run(String... args) {
    gridDbClient.createTableUser();
    log.info("Create table Users if not exists");
    UserRecord usr = usersContainerClient.getUserByEmail("one@test.com");
    log.info("Test find test user: " + usr);
    String defaultEmail = "admin@example.com";
    String regularUserEmail = "user@example.com";
    if (usersContainerClient.getUserByEmail(defaultEmail) == null) {
      UserRecord user =
          new UserRecord(
              UUID.randomUUID().toString(),
              defaultEmail,
              "Admin User",
              passwordEncoder.encode("admin123"));
      usersContainerClient.insert(user);
      log.info("Default admin user created with ID: " + user.id());
    }

    if (usersContainerClient.getUserByEmail(regularUserEmail) == null) {
      UserRecord user =
          new UserRecord(
              UUID.randomUUID().toString(),
              regularUserEmail,
              "Regular User",
              passwordEncoder.encode("user123"));
      usersContainerClient.insert(user);
      log.info("Regular user created with ID: " + user.id());
    }
  }
}
