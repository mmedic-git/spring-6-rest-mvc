package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
@WebMvcTest(BeerController.class)   //ovo sam promijenio 8.5.2023, umjesto @SpringBootTest ubacio @WebMvcTest ,
                                    // želimo ogrančiti testove samo na BeerController klasu.

class BeerControllerTest {

    // @Autowired
    // BeerController beerController;


    // Spring MockMVC allows you to test the controller interactions in a servlet context without the application running in a application server.

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;          //kada autowire-amo objectMapper SpringBoot će nam provajdati runtime context

    @MockBean
    BeerService beerService;  //@MockBean provide-a automatski sve potrebne dependcies, inače bi došlo do exceptiona-a

    BeerServiceImpl beerServiceImpl; //  = new BeerServiceImpl();ovo mičem jer sam dolje stavio @BeforeEach pa da ne radi 2 puta novi objekt

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testCreateBeerNullBeerName() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder().build();  //napravi prazan objekt

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false).get(1));

        /* idemo cijelu priču napravit malo informativniju sa MvcResult, da javimo calleru gdje je problem
        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO))).andExpect(status().isBadRequest());  // prije nego smo uveli validaciju podataka, ova metoda je fejlala, odnosno upis praznog objekta "u bazu" je prolazio uspješno

         */

        MvcResult mvcResult = mockMvc.perform(post(BeerController.BEER_PATH)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(jsonPath("$.length()", is(6)))   // 2 jer očekujem ovakav output "[{"beerName":"must not be null"},{"beerName":"must not be blank"}]"
                                    .andExpect(status().isBadRequest()).andReturn();  // prije nego smo uveli validaciju podataka, ova metoda je fejlala, odnosno upis praznog objekta "u bazu" je prolazio uspješno

        System.out.println(mvcResult.getResponse().getContentAsString());

    }

    @Test
    void testCreateNewBeer() throws Exception {

        /*

        Obzirom da smo gore stavili

        @Autowired
        ObjectMapper objectMapper;

        onda ne trebamo više "ručno" instancirati novu objectMapper instancu. POdsjećam @Autowired sam pripremi sve da bi se objekt inicijalizirao unutar SpringBoot context-a.

        ObjectMapper objectMapper = new ObjectMapper();  //testiramo dodavanje novog Beer objekta pomoću Jackson-a, JSON -> Java POJO and vice versa

        objectMapper.findAndRegisterModules(); //bez ovoga bi dobili exception, Jackson ne bi znao hendlati LocalDatetime datatype, ovako ih potraži po classpath-u

        */

        BeerDTO beer = beerServiceImpl.listBeers(null, null, false).get(0);
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false).get(1)); //za svaki Beer ovjekt koji se proslijedi metoda


        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        System.out.println(objectMapper.writeValueAsString(beer));

    }
    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    @Captor
    ArgumentCaptor<UUID>    uuidArgumentCaptor;

    @Test
    void testPatchBeer() throws  Exception{
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false).get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");

        // i ovo ćemo refaktorirati
        // mockMvc.perform(patch(BeerController.BEER_PATH + "/" + beer.getId())
        mockMvc.perform(patch(BeerController.BEER_PATH_ID , beer.getId())  //sad će se beer.getId() bindati u BEER_PATH_ID automatski, jer postoji overload-ana patch metoda koja može prihvatiti i ovakav način predaje argumenata
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());

        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());


    }


    @Test
    void testDeleteBeer() throws Exception {

        BeerDTO beer = beerServiceImpl.listBeers(null, null, false).get(0);

        given(beerService.deleteBeerById(any())).willReturn(true);

        // mockMvc.perform(delete(BeerController.BEER_PATH + "/" + beer.getId())
        mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Mockito ima ArgumentCaptor klasu, koja može uloviti proslijeđeni argument za pojedini tip objekta
        // ovo nam više ne treba jer smo gore deklarirali
        // @Captor
        // ArgumentCaptor<Beer> beerArgumentCaptor;
        // umjesto eksplicitne deklaracije
        // ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(beerService).deleteBeerById(uuidArgumentCaptor.capture());



        //provjeri da li se id piva podudara sa proslijeđenim utl-om, ako nisu isti dobit ćemo exception
        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());


        // morali bi biti isti
        System.out.println(uuidArgumentCaptor.getValue());  //ispiši ID izbrisanog beer objekta
        System.out.println(beer.getId());




    }

    @Test
     void testUpdateBeer() throws Exception{

        BeerDTO beer = beerServiceImpl.listBeers(null, null, false).get(0);

        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));  //ovo je neki trik, da se ne mora zapravo napraviti pravi update, nego samo vratiti objekt -> NIJE MI OVO BAŠ JASNO, ali bez toga puca

        // mockMvc.perform(put(BeerController.BEER_PATH + "/" + beer.getId())
        mockMvc.perform(put(BeerController.BEER_PATH_ID,  beer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }


    @Test
    void testUpdateBeerBlankName() throws Exception {

        BeerDTO beer = beerServiceImpl.listBeers(null, null, false).get(0);
        beer.setBeerName(""); //namjerno podmetnemo empty string

        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));  //ovo je neki trik, da se ne mora zapravo napraviti pravi update, nego samo vratiti objekt -> NIJE MI OVO BAŠ JASNO, ali bez toga puca

        // mockMvc.perform(put(BeerController.BEER_PATH + "/" + beer.getId())
        mockMvc.perform(put(BeerController.BEER_PATH_ID,  beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));

        // ovo nam ne treba za test "blank name" verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testListBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any())).willReturn(beerServiceImpl.listBeers(null, null, false));

        mockMvc.perform(get(BeerController.BEER_PATH )
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()",is(3)));      // obzirom da su svi UUID-ovi generirani na pokretanju, stavili smo uvjet da su u listi 3 piva, š
                                                                              // to znamo da jesu na inicijalizaciji programa

        
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        // nakon refactoringa sa Optional
        // given(beerService.getBeerById((any(UUID.class)))).willThrow(NotFoundException.class);

        given(beerService.getBeerById((any(UUID.class)))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());

    }

    @Test
    void getBeerById() throws Exception {

        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, false).get(0);

        // given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer); //reći ćemo Mockito-u da za bilo koji UUID vrati naš odabrani test beer sa indeksom 0 iz liste
        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(MockMvcRequestBuilders
                // .get("/api/v1/beer/" +UUID.randomUUID())
                // .get(BeerController.BEER_PATH + "/" + testBeer.getId())
                .get(BeerController.BEER_PATH_ID,testBeer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))           //da postrožim malo uvjete testa, mora vratiti nešto što nije empty response
                .andExpect(jsonPath("$.id",is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));

        // System.out.println(beerController.getBeerById(UUID.randomUUID()));
    }
}