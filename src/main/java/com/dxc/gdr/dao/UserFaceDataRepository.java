package com.dxc.gdr.dao;


import com.dxc.gdr.model.User;
import com.dxc.gdr.model.UserFaceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFaceDataRepository extends JpaRepository<UserFaceData, Long> {
    Optional<UserFaceData> findByUser(User user);
}