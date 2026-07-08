package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.infrastructure.config.EfsStorageProperties;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalEfsStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalEfsStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        EfsStorageProperties properties = new EfsStorageProperties();
        properties.setBasePath(tempDir.toString());
        adapter = new LocalEfsStorageAdapter(properties);
    }

    @Test
    void writesFileAndCreatesDirectories() {
        byte[] content = "pdf-content".getBytes();

        String absolutePath = adapter.write("guides/2026-06-02/transportes-rapidos/guide-1.pdf", content);

        // Creamos la ruta esperada de forma 100% segura para evitar choques entre los "/" de Linux y los "\" de Windows
        Path expectedPath = tempDir.resolve(Path.of("guides", "2026-06-02", "transportes-rapidos", "guide-1.pdf"));

        // Verificamos que el archivo realmente se escribió en el disco temporal
        assertTrue(Files.exists(expectedPath));
    }

    @Test
    void readsWrittenFile() {
        byte[] content = "pdf-content".getBytes();
        String absolutePath = adapter.write("guides/test.pdf", content);

        byte[] readContent = adapter.read(absolutePath);

        assertArrayEquals(content, readContent);
    }

    @Test
    void throwsNotFoundWhenFileDoesNotExist() {
        Path missingPath = tempDir.resolve("missing.pdf");

        DomainError error = assertThrows(DomainError.class, () -> adapter.read(missingPath.toString()));

        assertEquals(DomainError.Type.NOT_FOUND, error.getType());
    }

    @Test
    void rejectsPathOutsideBaseDirectory() {
        assertThrows(DomainError.class, () -> adapter.write("../outside.pdf", "x".getBytes()));
    }
}