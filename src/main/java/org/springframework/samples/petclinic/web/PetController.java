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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp"));
    private final ClinicService clinicService;

    public PetController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {
        return this.clinicService.findPetTypes();
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.clinicService.findOwnerById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
    }

    @GetMapping(value = "/pets/new")
    public String initCreationForm(Owner owner, ModelMap model) {
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model,
            @RequestParam(required = false) MultipartFile photoFile) {
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null){
            result.rejectValue("name", "duplicate", "already exists");
        }
        if (validatePhoto(photoFile, result)) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                pet.setPhoto(photoFile.getBytes());
            } catch (IOException e) {
                result.reject("photo", "Error reading photo file");
                model.put("pet", pet);
                return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
            }
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return "redirect:/owners/{ownerId}";
    }

    @GetMapping(value = "/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
        Pet pet = this.clinicService.findPetById(petId);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model,
            @RequestParam(required = false) MultipartFile photoFile) {
        if (validatePhoto(photoFile, result)) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                pet.setPhoto(photoFile.getBytes());
            } catch (IOException e) {
                result.reject("photo", "Error reading photo file");
                model.put("pet", pet);
                return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
            }
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return "redirect:/owners/{ownerId}";
    }

    private boolean validatePhoto(MultipartFile photoFile, BindingResult result) {
        if (photoFile == null || photoFile.isEmpty()) {
            return false;
        }
        if (photoFile.getSize() > MAX_PHOTO_SIZE) {
            result.reject("photo", "Photo size must not exceed 5MB");
            return true;
        }
        String contentType = photoFile.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            result.reject("photo", "Only JPEG, PNG, GIF, and WebP images are allowed");
            return true;
        }
        return false;
    }

}
