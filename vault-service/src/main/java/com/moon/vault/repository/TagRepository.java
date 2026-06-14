package com.moon.vault.repository;

import com.moon.vault.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserCodeOrderByNameAsc(String userCode);

    Optional<Tag> findByTagCodeAndUserCode(String tagCode, String userCode);

    List<Tag> findByTagCodeInAndUserCode(Collection<String> tagCodes, String userCode);

    boolean existsByNameAndUserCode(String name, String userCode);
}
