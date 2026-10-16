package com.eruden.foldersdemo.service.validator;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FolderServiceValidatorTest {

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private FolderServiceValidator validator;

    @Test
    void validateCreation_passes_whenParentIdIsNull() {
        // Arrange
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(null).build();

        // Act + Assert
        assertThatCode(() -> validator.validateCreation(request)).doesNotThrowAnyException();
        verifyNoInteractions(folderRepository);
    }

    @Test
    void validationCreation_passes_whenParentExists() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(1L).build();
        when(folderRepository.findById(1L)).thenReturn(Optional.of(new Folder()));

        assertThatCode(() -> validator.validateCreation(request)).doesNotThrowAnyException();
    }

    @Test
    void validationCreation_passes_whenParentIsDifferentFolder() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(3L).build();

        when(folderRepository.findById(3L)).thenReturn(Optional.of(new Folder()));
        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_passes_whenParentIdIsNull() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(null).build();

        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
        verifyNoInteractions(folderRepository);
    }
    @Test
    void validationCreation_throws_whenParentIdIsNotPresent() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(99L).build();

        when(folderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> validator.validateCreation(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Parent does not exist");
    }

    @Test
    void validateUpdate_throws_whenFolderIsItsOwnParent() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(5L).build();

        when(folderRepository.findById(5L)).thenReturn(Optional.of(new Folder()));

        assertThatThrownBy(() -> validator.validateUpdate(request, 5L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("A folder cannot be its own parent");
    }
}
