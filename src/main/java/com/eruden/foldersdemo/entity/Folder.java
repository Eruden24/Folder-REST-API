package com.eruden.foldersdemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="folder")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Folder {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="parent_id")
    private Long parentId;

    @Column(name="subject")
    private String subject;
}
