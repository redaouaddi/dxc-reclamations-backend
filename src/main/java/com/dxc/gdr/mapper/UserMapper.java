package com.dxc.gdr.mapper;

import com.dxc.gdr.Dto.UserDto;
import com.dxc.gdr.model.Access;
import com.dxc.gdr.model.Gender;
import com.dxc.gdr.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "gender", source = "gender", qualifiedByName = "genderToString")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserDto toDto(User user);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "stringToGender")
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserDto dto);

    @Named("genderToString")
    default String genderToString(Gender gender) {
        return gender != null ? gender.name() : null;
    }

    @Named("stringToGender")
    default Gender stringToGender(String gender) {
        return gender != null ? Gender.valueOf(gender) : null;
    }

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Access> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Access::getName)
                .collect(Collectors.toSet());
    }
}
