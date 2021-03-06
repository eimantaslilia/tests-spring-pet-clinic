package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class OwnerControllerTest {

    @Autowired
    OwnerController ownerController;

    @Autowired
    ClinicService clinicService;

    MockMvc mockMvc;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    @AfterEach
    void tearDown(){
        reset(clinicService);
    }

    @Test
    void newOwnerPostValid() throws Exception{
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Jimmy")
            .param("lastName", "Buffalo")
            .param("address", "145 Hell St")
            .param("city", "Lincolnshire")
            .param("telephone", "12544565"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void newOwnerPostNotValid() throws Exception{
        mockMvc.perform(post("/owners/new")
                .param("firstName", "Jimmy")
                .param("lastName", "Buffalo")
                .param("City", "Lincolnshire"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("owner", "address"))
                .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testProcessUpdateOwnerFormSuccess() throws Exception{
        mockMvc.perform(post("/owners/{ownerId}/edit", 6)
                .param("firstName", "Billy")
                .param("lastName", "Post")
                .param("address", "12 Lincoln St")
                .param("city", "Birmingham")
                .param("telephone", "99987458"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateOwnerFormFailure() throws Exception{
        mockMvc.perform(post("/owners/6/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testFindByNameNotFound() throws Exception{
        mockMvc.perform(get("/owners")
                    .param("lastName", "Dont Find Me"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"));
    }

    @Test
    void testReturnListOfOwners() throws Exception{
        given(clinicService.findOwnerByLastName("")).willReturn(Lists.newArrayList(new Owner(), new Owner()));
        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"));

        then(clinicService).should().findOwnerByLastName(stringArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("");
    }

    @Test
    void testFindOneOwner() throws Exception{

        Owner owner = new Owner();
        owner.setId(5);
        String findJustOne = "FindJustOne";
        owner.setLastName(findJustOne);
        given(clinicService.findOwnerByLastName(findJustOne)).willReturn(Lists.newArrayList(owner));
        mockMvc.perform(get("/owners")
                    .param("lastName", findJustOne))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/5"));

        then(clinicService).should().findOwnerByLastName(anyString());
    }

    @Test
    void initCreationFormTest() throws Exception{

        mockMvc.perform(get("/owners/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("owner"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));

    }
}