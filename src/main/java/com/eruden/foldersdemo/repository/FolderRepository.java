package com.eruden.foldersdemo.repository;

import com.eruden.foldersdemo.entity.Folder;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByParentFolderId(Long parentFolderId);

    List<Folder> findAllByParentFolderIsNull();
}

