package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
// @RequestMapping("/api/v1/beer")  // ovdje smo na cijelu klasu postavili osnovno url za API
// u varijanti refactoringa ovo mičemo jer sad imamo BEER_PATH i BEER_PATH_ID varijable, pa ne moramo postavljati "osnovni" path na razini cijele klase
public class BeerController {

    public static final     String BEER_PATH = "/api/v1/beer";

    public static final     String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final           BeerService beerService;

    // radimo refactoring preko constant varijabli @PatchMapping("{beerId}")
    @PatchMapping(value = BEER_PATH_ID)
    public ResponseEntity updateBeerPatchById(@PathVariable("beerId") UUID beerId, @RequestBody BeerDTO beer) {

        beerService.patchBeerById(beerId, beer);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    // refactoring @DeleteMapping("{beerId}")
    @DeleteMapping(value = BEER_PATH_ID)
    public ResponseEntity deleteByID(@PathVariable("beerId") UUID beerID) {

        if(! beerService.deleteBeerById(beerID)) {
            throw new NotFoundException();
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    // @PutMapping("{beerId}")
    @PutMapping(value = BEER_PATH_ID)
    public ResponseEntity updateById(@PathVariable("beerId") UUID beerID, @RequestBody BeerDTO beer) {

        if (beerService.updateBeerById(beerID, beer).isEmpty()) {

            throw new NotFoundException();
        };

        return new  ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = BEER_PATH)
    // @RequestMapping (method = RequestMethod.POST)  // @PostMapping je isto što i  @RequestMapping (method = RequestMethod.POST) samo kraće napisano :-)
    public ResponseEntity  handlePost(@Validated @RequestBody BeerDTO beer)  {                    // sa @RequestBody kažemo da JSON BODY iz posta veže/mapira na varijablu beer (tipa Beer)



        BeerDTO savedBeer = beerService.saveNewBeer(beer);

        //nakon snimanja trebao bi dobiti neki id
        System.out.println(savedBeer);


        //iako je savršeno ok prihvatit beer objekt i snimit ga u Map kolekciju, pozivatelju vratiti 201, ipak ćemo dodati par headera sa dodatnim informacijama za pozivatelja

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    //refactoring @RequestMapping(method = RequestMethod.GET)     // bolje da osnovni "RequestMapping" dignemo na "cijelu klasu", kao osnovni endpoint "/api/v1/beer", pa nam value ne treba,
                                                    // jer se automatski naslijeđuje "osnovni" mapping "/api/v1/beer"
                                                    // Dakle, isto je kao da piše : @RequestMapping(value = "/api/v1/beer/", method = RequestMethod.GET)

    // @RequestMapping("/api/v1/beer")      // dakle, ovime smo rekli spring da kad dobije ovakav url, odgovori na njega tako da pošalje listu piva iz "baze" odnosno Map kolekcije
    @GetMapping(value = BEER_PATH) //možemo korisitit @GetMapping umjesto @RequestMapping(method = RequestMethod.GET)
    public List<BeerDTO> listBeers() {         // Jackson je iz "obične liste" piva proizveo JSON response
        return beerService.listBeers();
    }

    // refactoring @RequestMapping(value = "{beerID}", method = RequestMethod.GET)      //obzirom da naslijeđuje "osnovni dio", ostaje nam još samo parametar. To je ekvivalent @RequestMapping(value = "/api/v1/beer/{beerID}", method = RequestMethod.GET) da nema "osnovnog mapiranja" na razini klase
    // @RequestMapping(value = "/api/v1/beer/{beerID}", method = RequestMethod.GET)  //želimo da se ova metoda invoka samo za Get pozive, ostale ignoriraj

    /*

    napravit ćemo refactoring, umjesto lokalnog BeerController exception handlera, uvodimo globalni na razini ExceptionController klase

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException() {

        System.out.println("In exception handler");

        return ResponseEntity.notFound().build();  //ovim će nam test getBeerByIdNotFound() završit uspješno iz razloga što smo shendlali NotFoundException

    }

     */

    @GetMapping(value = BEER_PATH_ID)
    public BeerDTO getBeerById(@PathVariable("beerId") UUID beerID) {  // možda bi Spring i sam matchirao beerID varijable iz @RequestMappinga i ovu dolje, ali bolje mu eksplicitno to naznačiti preko @PathVariable

        log.debug("Get Beer by Id - in controller");

        return beerService.getBeerById(beerID).orElseThrow(NotFoundException::new);
    }
}
