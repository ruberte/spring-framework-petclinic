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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private static final Logger logger = Logger.getLogger(PetController.class.getName());
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
        if (!savePhoto(pet, photoFile, result)) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
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
        if (!savePhoto(pet, photoFile, result)) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return "redirect:/owners/{ownerId}";
    }

    private boolean savePhoto(Pet pet, MultipartFile photoFile, BindingResult result) {
        if (photoFile == null || photoFile.isEmpty()) {
            return true;
        }
        if (photoFile.getSize() > MAX_PHOTO_SIZE) {
            result.reject("photo", "Photo size must not exceed 5MB");
            return false;
        }
        String contentType = photoFile.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            result.reject("photo", "Only JPEG, PNG, GIF, and WebP images are allowed");
            return false;
        }
        if (!isValidImageMagic(photoFile)) {
            result.reject("photo", "Invalid image file format");
            return false;
        }
        try {
            pet.setPhoto(photoFile.getBytes());
        } catch (IOException e) {
            logger.severe("Photo upload failed for pet " + pet.getId() + ": " + e.getMessage());
            result.reject("photo", "Error reading photo file");
            return false;
        }
        return true;
    }

    private boolean isValidImageMagic(MultipartFile file) {
        try {
            byte[] header = new byte[8];
            file.getInputStream().read(header);
            return (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) || // JPEG
                   (header[0] == (byte) 0x89 && header[1] == 0x50) ||       // PNG
                   (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46) || // GIF
                   (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[4] == 0x57); // WebP
        } catch (IOException e) {
            return false;
        }
    }

    @GetMapping(value = "/pets/{petId}/photo")
    public ResponseEntity<byte[]> getPetPhoto(@PathVariable("petId") int petId) {
        Pet pet = this.clinicService.findPetById(petId);
        if (pet == null || pet.getPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(pet.getPhoto(), headers, HttpStatus.OK);
    }

}
