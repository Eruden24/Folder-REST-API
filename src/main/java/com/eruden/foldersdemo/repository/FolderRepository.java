package com.eruden.foldersdemo.repository;

import com.eruden.foldersdemo.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

}

