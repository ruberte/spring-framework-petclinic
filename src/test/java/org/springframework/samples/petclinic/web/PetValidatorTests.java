package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PetValidatorTests {

    private PetValidator validator;

    @BeforeEach
    void setUp() {
        this.validator = new PetValidator();
    }

    @Test
    void shouldAcceptValidMicrochipId() {
        Pet pet = createValidPet();
        pet.setMicrochipId("ABC123XYZ789");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldAcceptEmptyMicrochipId() {
        Pet pet = createValidPet();
        pet.setMicrochipId("");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldAcceptNullMicrochipId() {
        Pet pet = createValidPet();
        pet.setMicrochipId(null);

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldRejectMicrochipIdTooLong() {
        Pet pet = createValidPet();
        pet.setMicrochipId("1234567890123456");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("microchipId")).isEqualTo(1);
        assertThat(errors.getFieldError("microchipId").getCode()).isEqualTo("maxLength");
    }

    @Test
    void shouldRejectMicrochipIdWithSpecialCharacters() {
        Pet pet = createValidPet();
        pet.setMicrochipId("ABC-123-XYZ");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("microchipId")).isEqualTo(1);
        assertThat(errors.getFieldError("microchipId").getCode()).isEqualTo("invalidFormat");
    }

    @Test
    void shouldRejectMicrochipIdWithSpaces() {
        Pet pet = createValidPet();
        pet.setMicrochipId("ABC 123 XYZ");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("microchipId")).isEqualTo(1);
        assertThat(errors.getFieldError("microchipId").getCode()).isEqualTo("invalidFormat");
    }

    private Pet createValidPet() {
        Pet pet = new Pet();
        pet.setName("TestPet");
        pet.setBirthDate(LocalDate.now());
        PetType type = new PetType();
        type.setId(1);
        type.setName("dog");
        pet.setType(type);
        return pet;
    }

}
