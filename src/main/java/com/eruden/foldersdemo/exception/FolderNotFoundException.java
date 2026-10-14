package com.eruden.foldersdemo.exception;

public class FolderNotFoundException extends RuntimeException {

    public FolderNotFoundException(Long id) {
        super("Folder not found with id " + id);
    }
}
