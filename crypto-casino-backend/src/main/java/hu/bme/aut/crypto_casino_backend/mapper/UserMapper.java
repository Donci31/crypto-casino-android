package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserRegistrationDto;
import hu.bme.aut.crypto_casino_backend.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "password", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  UserDto toDto(User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "wallets", ignore = true)
  User fromRegistrationDto(UserRegistrationDto registrationDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "wallets", ignore = true)
  void updateFromDto(UserDto dto, @MappingTarget User user);

}
