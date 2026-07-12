package com.dispatchflow.guides.unit.domain.valueobjects;

import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void createsValidEmail() {
        Email email = Email.create("responsable@empresa.cl");

        assertEquals("responsable@empresa.cl", email.value());
    }

    @Test
    void rejectsBlankEmail() {
        DomainError error = assertThrows(DomainError.class, () -> Email.create("  "));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void rejectsInvalidFormat() {
        assertThrows(DomainError.class, () -> Email.create("not-an-email"));
    }
}
