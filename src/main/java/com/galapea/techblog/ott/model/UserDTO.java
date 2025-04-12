package com.galapea.techblog.ott.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDTO {

  @Size(max = 255)
  private String id;

  @NotNull
  @Size(max = 255)
  @UserEmailUnique
  private String email;

  @Size(max = 255)
  private String name;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
