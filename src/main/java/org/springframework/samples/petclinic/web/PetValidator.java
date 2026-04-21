/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import org.jspecify.annotations.NullMarked;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * <code>Validator</code> for <code>Pet</code> forms.
 * <p>
 * We're not using Bean Validation annotations here because it is easier to define such validation rule in Java.
 * </p>
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@NullMarked
public class PetValidator implements Validator {

    private static final String REQUIRED = "required";
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[\\w.-]+(:[0-9]+)?(/[\\w./?%&=-]*)?$"
    );
    private static final Pattern MICROCHIP_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    @Override
    public void validate(Object obj, Errors errors) {
        Pet pet = (Pet) obj;
        String name = pet.getName();
        // name validation
        if (!StringUtils.hasLength(name)) {
            errors.rejectValue("name", REQUIRED, REQUIRED);
        }

        // type validation
        if (pet.isNew() && pet.getType() == null) {
            errors.rejectValue("type", REQUIRED, REQUIRED);
        }

        // birth date validation
        if (pet.getBirthDate() == null) {
            errors.rejectValue("birthDate", REQUIRED, REQUIRED);
        }

        // photo URL validation
        String photoUrl = pet.getPhotoUrl();
        if (StringUtils.hasLength(photoUrl)) {
            if (photoUrl.length() > 500) {
                errors.rejectValue("photoUrl", "maxLength", "Photo URL must not exceed 500 characters");
            }
            if (!URL_PATTERN.matcher(photoUrl).matches()) {
                errors.rejectValue("photoUrl", "invalidFormat", "Photo URL must be a valid HTTP/HTTPS URL");
            }
        }

        // microchip ID validation
        String microchipId = pet.getMicrochipId();
        if (StringUtils.hasLength(microchipId)) {
            if (microchipId.length() > 15) {
                errors.rejectValue("microchipId", "maxLength", "Microchip ID must not exceed 15 characters");
            }
            if (!MICROCHIP_PATTERN.matcher(microchipId).matches()) {
                errors.rejectValue("microchipId", "invalidFormat", "Microchip ID must contain only letters and numbers");
            }
        }
    }

    /**
     * This Validator validates *just* Pet instances
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Pet.class.isAssignableFrom(clazz);
    }


}
