package com.moon.vault.repository;

import com.moon.vault.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserCodeAndWorkflowStateOrderBySortOrderAscCreateDateAsc(String userCode, String workflowState);

    Optional<Folder> findByFolderCodeAndUserCode(String folderCode, String userCode);

    boolean existsByFolderCodeAndUserCode(String folderCode, String userCode);
}
