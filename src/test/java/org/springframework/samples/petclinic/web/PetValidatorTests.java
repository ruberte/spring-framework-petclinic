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

    @Test
    void shouldAcceptMicrochipIdWithExactly15Characters() {
        Pet pet = createValidPet();
        pet.setMicrochipId("123456789012345");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldRejectWhitespaceOnlyMicrochipId() {
        Pet pet = createValidPet();
        pet.setMicrochipId("   ");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("microchipId")).isEqualTo(1);
        assertThat(errors.getFieldError("microchipId").getCode()).isEqualTo("invalidFormat");
    }

    @Test
    void shouldTrimWhitespaceFromMicrochipId() {
        Pet pet = createValidPet();
        pet.setMicrochipId("  ABC123  ");

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(pet.getMicrochipId()).isEqualTo("ABC123");
    }

    @Test
    void shouldAcceptValidWeight() {
        Pet pet = createValidPet();
        pet.setWeight(new java.math.BigDecimal("5.50"));

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldAcceptNullWeight() {
        Pet pet = createValidPet();
        pet.setWeight(null);

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void shouldRejectZeroWeight() {
        Pet pet = createValidPet();
        pet.setWeight(java.math.BigDecimal.ZERO);

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("weight")).isEqualTo(1);
        assertThat(errors.getFieldError("weight").getCode()).isEqualTo("invalidValue");
    }

    @Test
    void shouldRejectNegativeWeight() {
        Pet pet = createValidPet();
        pet.setWeight(new java.math.BigDecimal("-5.00"));

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("weight")).isEqualTo(1);
        assertThat(errors.getFieldError("weight").getCode()).isEqualTo("invalidValue");
    }

    @Test
    void shouldRejectWeightExceedingMaximum() {
        Pet pet = createValidPet();
        pet.setWeight(new java.math.BigDecimal("1000.00"));

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.getFieldErrorCount("weight")).isEqualTo(1);
        assertThat(errors.getFieldError("weight").getCode()).isEqualTo("invalidValue");
    }

    @Test
    void shouldAcceptWeightAtMaximum() {
        Pet pet = createValidPet();
        pet.setWeight(new java.math.BigDecimal("999.99"));

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        this.validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
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
