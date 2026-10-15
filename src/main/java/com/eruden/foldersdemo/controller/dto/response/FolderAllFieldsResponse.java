package com.eruden.foldersdemo.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderAllFieldsResponse {

    private Long id;
    private Long parentId;
    private String subject;
    private Timestamp created;
    private Timestamp updated;
}
