package com.eruden.foldersdemo.repository;

import com.eruden.foldersdemo.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
