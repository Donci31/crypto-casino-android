package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.UserDto;
import hu.bme.aut.crypto_casino_backend.dto.UserRegistrationDto;
import hu.bme.aut.crypto_casino_backend.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WalletMapper.class})
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "wallet", source = "wallet", qualifiedByName = "toWalletSummary")
    @Mapping(target = "kycStatus", expression = "java(user.getKycStatus() != null ? user.getKycStatus().name() : null)")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "wallet", source = "wallet", qualifiedByName = "walletSummaryToWallet")
    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "kycStatus", constant = "NOT_STARTED")
    User fromRegistrationDto(UserRegistrationDto registrationDto);

    List<UserDto> toDtoList(List<User> users);

    @AfterMapping
    default void setKycStatusEnum(@MappingTarget User user, UserDto userDto) {
        if (userDto.getKycStatus() != null) {
            try {
                user.setKycStatus(User.KycStatus.valueOf(userDto.getKycStatus()));
            } catch (IllegalArgumentException e) {
                // Log or handle invalid enum values
            }
        }
    }
}
