package com.dxc.gdr.mapper;

import com.dxc.gdr.Dto.AccessDto;
import com.dxc.gdr.model.Access;
import com.dxc.gdr.model.EPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccessMapper {

    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "permissionsToNames")
    AccessDto toDto(Access access);

    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "namesToPermissions")
    Access toEntity(AccessDto dto);

    @Named("permissionsToNames")
    default Set<String> permissionsToNames(Set<EPermission> permissions) {
        if (permissions == null) return null;
        return permissions.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Named("namesToPermissions")
    default Set<EPermission> namesToPermissions(Set<String> names) {
        if (names == null) return null;
        return names.stream()
                .map(EPermission::valueOf)
                .collect(Collectors.toSet());
    }
}
