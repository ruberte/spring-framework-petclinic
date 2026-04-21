package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class PetControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private PetController petController;

    @Autowired
    private FormattingConversionServiceFactoryBean formattingConversionServiceFactoryBean;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(petController)
            .setConversionService(formattingConversionServiceFactoryBean.getObject())
            .build();

        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("hamster");
        given(this.clinicService.findPetTypes()).willReturn(Lists.newArrayList(cat));

        Owner owner = new Owner();
        owner.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);
        given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(new Pet());
    }

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"))
            .andExpect(model().attributeExists("pet"));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("pet"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testProcessUpdateFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testCreationFormWithValidPhoto() throws Exception {
        byte[] pngContent = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
        MockMultipartFile photoFile = new MockMultipartFile(
            "photoFile", "photo.png", "image/png", pngContent
        );
        mockMvc.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .file(photoFile)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testCreationFormWithOversizedPhoto() throws Exception {
        byte[] largeFile = new byte[6 * 1024 * 1024];
        MockMultipartFile photoFile = new MockMultipartFile(
            "photoFile", "photo.png", "image/png", largeFile
        );
        mockMvc.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .file(photoFile)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testCreationFormWithInvalidMimeType() throws Exception {
        MockMultipartFile photoFile = new MockMultipartFile(
            "photoFile", "file.txt", "text/plain", "not an image".getBytes()
        );
        mockMvc.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .file(photoFile)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testCreationFormWithValidPhotoUrl() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
            .param("photoUrl", "https://example.com/pet.jpg")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testCreationFormWithInvalidPhotoUrl() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
            .param("photoUrl", "not-a-valid-url")
        )
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(model().attributeHasFieldErrors("pet", "photoUrl"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testCreationFormWithOversizedPhotoUrl() throws Exception {
        String longUrl = "https://example.com/" + "a".repeat(500);
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
            .param("photoUrl", longUrl)
        )
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(model().attributeHasFieldErrors("pet", "photoUrl"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    void testGetPetPhoto_whenPhotoExists_jpeg() throws Exception {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setPhoto(new byte[]{(byte)0xFF, (byte)0xD8, 0x01, 0x02});
        owner.addPet(pet);
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(new byte[]{(byte)0xFF, (byte)0xD8, 0x01, 0x02}));
    }

    @Test
    void testGetPetPhoto_whenPhotoExists_png() throws Exception {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setPhoto(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});
        owner.addPet(pet);
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void testGetPetPhoto_whenPhotoExists_gif() throws Exception {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setPhoto(new byte[]{0x47, 0x49, 0x46, 0x38});
        owner.addPet(pet);
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_GIF));
    }

    @Test
    void testGetPetPhoto_whenPhotoNotExists() throws Exception {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setPhoto(null);
        owner.addPet(pet);
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetPetPhoto_whenPetNotExists() throws Exception {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetPetPhoto_whenOwnerNotExists() throws Exception {
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(null);

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void testProcessCreationFormWithColorAndBreed() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
            .param("color", "Brown")
            .param("breed", "Syrian Hamster")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateFormWithColorAndBreed() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
            .param("color", "Golden")
            .param("breed", "Golden Retriever")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

}
