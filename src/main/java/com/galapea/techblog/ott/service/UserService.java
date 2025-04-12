package com.galapea.techblog.ott.service;

import com.galapea.techblog.ott.model.UserDTO;
import com.galapea.techblog.ott.model.UserRecord;
import com.galapea.techblog.ott.util.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UsersContainerClient usersContainerClient;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      final UsersContainerClient usersContainerClient, final PasswordEncoder passwordEncoder) {
    this.usersContainerClient = usersContainerClient;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserDTO> findAll() {
    final List<UserRecord> users = usersContainerClient.queryUsers();
    return users.stream().map(user -> mapToDTO(user, new UserDTO())).toList();
  }

  public UserDTO get(final String id) {
    UserRecord userRecord = usersContainerClient.getUserById(id);
    return Optional.ofNullable(userRecord)
        .map(user -> mapToDTO(user, new UserDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UserDTO getByEmail(final String email) {
    UserRecord userRecord = usersContainerClient.getUserByEmail(email);
    if (userRecord == null) {
      throw new NotFoundException();
    }
    UserDTO user = new UserDTO();
    user.setId(userRecord.id());
    user.setEmail(userRecord.email());
    user.setName(userRecord.name());
    return user;
  }

  public String create(final UserDTO userDTO) {
    UserRecord user =
        new UserRecord(
            UUID.randomUUID().toString(),
            userDTO.getEmail(),
            userDTO.getName(),
            passwordEncoder.encode(UUID.randomUUID().toString()));
    usersContainerClient.insert(user);
    return user.id();
  }

  public void update(final String id, final UserDTO userDTO) {
    UserRecord user =
        new UserRecord(
            userDTO.getId(),
            userDTO.getEmail(),
            userDTO.getName(),
            passwordEncoder.encode(UUID.randomUUID().toString()));
    usersContainerClient.update(user);
  }

  public void delete(final String id) {
    usersContainerClient.deleteUser(id);
  }

  private UserDTO mapToDTO(final UserRecord user, final UserDTO userDTO) {
    userDTO.setId(user.id());
    userDTO.setEmail(user.email());
    userDTO.setName(user.name());
    return userDTO;
  }

  public boolean idExists(final String id) {
    return usersContainerClient.getUserById(id) != null;
  }

  public boolean emailExists(final String email) {
    return usersContainerClient.getUserByEmail(email) != null;
  }
}
