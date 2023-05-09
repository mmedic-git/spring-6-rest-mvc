package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpLogging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/beer")  // ovdje smo na cijelu klasu postavili osnovno url za API
public class BeerController {

    private final BeerService beerService;

    @PatchMapping("{beerId}")
    public ResponseEntity updateBeerPatchById(@PathVariable("beerId") UUID beerId, @RequestBody Beer beer) {

        beerService.patchBeerById(beerId, beer);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("{beerId}")
    public ResponseEntity deleteByID(@PathVariable("beerId") UUID beerID) {

        beerService.deleteBeerById(beerID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{beerId}")
    public ResponseEntity updateById(@PathVariable("beerId") UUID beerID, @RequestBody Beer beer) {

        beerService.updateBeerById(beerID, beer);

        return new  ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping
    // @RequestMapping (method = RequestMethod.POST)  // @PostMapping je isto što i  @RequestMapping (method = RequestMethod.POST) samo kraće napisano :-)
    public ResponseEntity  handlePost(@RequestBody Beer beer)  {                    // sa @RequestBody kažemo da JSON BODY iz posta veže/mapira na varijablu beer (tipa Beer)



        Beer savedBeer = beerService.saveNewBeer(beer);

        //nakon snimanja trebao bi dobiti neki id
        System.out.println(savedBeer);


        //iako je savršeno ok prihvatit beer objekt i snimit ga u Map kolekciju, pozivatelju vratiti 201, ipak ćemo dodati par headera sa dodatnim informacijama za pozivatelja

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)     // bolje da osnovni "RequestMapping" dignemo na "cijelu klasu", kao osnovni endpoint "/api/v1/beer", pa nam value ne treba,
                                                    // jer se automatski naslijeđuje "osnovni" mapping "/api/v1/beer"
                                                    // Dakle, isto je kao da piše : @RequestMapping(value = "/api/v1/beer/", method = RequestMethod.GET)

    // @RequestMapping("/api/v1/beer")      // dakle, ovime smo rekli spring da kad dobije ovakav url, odgovori na njega tako da pošalje listu piva iz "baze" odnosno Map kolekcije
    public List<Beer> listBeers() {         // Jackson je iz "obične liste" piva proizveo JSON response
        return beerService.listBeers();
    }

    @RequestMapping(value = "{beerID}", method = RequestMethod.GET)      //obzirom da naslijeđuje "osnovni dio", ostaje nam još samo parametar. To je ekvivalent @RequestMapping(value = "/api/v1/beer/{beerID}", method = RequestMethod.GET) da nema "osnovnog mapiranja" na razini klase
    // @RequestMapping(value = "/api/v1/beer/{beerID}", method = RequestMethod.GET)  //želimo da se ova metoda invoka samo za Get pozive, ostale ignoriraj
    public Beer getBeerById(@PathVariable("beerID") UUID beerID) {  // možda bi Spring i sam matchirao beerID varijable iz @RequestMappinga i ovu dolje, ali bolje mu eksplicitno to naznačiti preko @PathVariable

        log.debug("Get Beer by Id - in controller");

        return beerService.getBeerById(beerID);
    }
}
