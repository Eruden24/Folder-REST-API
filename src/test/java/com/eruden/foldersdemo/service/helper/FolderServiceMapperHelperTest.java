package com.eruden.foldersdemo.service.helper;

import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceMapperHelperTest {

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private FolderServiceMapperHelper helper;

    @Test
    void parentIdToParentFolder_returnsNull_whenParentIdIsNull() {
        assertThat(helper.parentIdToParentFolder(null)).isNull();
        verifyNoInteractions(folderRepository);
    }

    @Test
    void parentIdToParentFolder_returnsTheFolder_whenItExists() {
        Folder parent = new Folder();
        when(folderRepository.findById(3L)).thenReturn(Optional.of(parent));

        assertThat(helper.parentIdToParentFolder(3L)).isSameAs(parent);
    }

    @Test
    void parentIdToParentFolder_throws_whenFolderDoesNotExist() {
        when(folderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.parentIdToParentFolder(99L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Parent id not found!");
    }
}
