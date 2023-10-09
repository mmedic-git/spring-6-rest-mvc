package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary //obzirom da je ovo 2. implementacija iste klase BeerService, jedna mora biti @Primary, obzirom da su obje na Classpath-u. Na ovaj način ova klasa dobiva prednost nad onom
         // iz BeerServiceImpl implementacije . Ta originalna klasa radi sa H2 inmemory i sadrži 3 objekta, ručno napravljena, a ova radi sa mysql bazom podataka
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory) {

        List<Beer> beerList;

        if (StringUtils.hasText(beerName) && beerStyle == null) {

            beerList = listBeersByName(beerName);
        }

        else if (!StringUtils.hasText(beerName) && (beerStyle != null)) {
            beerList = listBeersByStyle(beerStyle);
        }

        else if (StringUtils.hasText(beerName) && (beerStyle != null)) {

            beerList = listBeersByBeersNameAndStyle (beerName, beerStyle);

            }

        else {
            beerList = beerRepository.findAll();
        }

        if (showInventory != null && !showInventory) {

            beerList.forEach(beer -> beer.setQuantityOnHand(null));
        }


        // return beerRepository

        return beerList.stream()
                .map(beerMapper::beerToBeerDTO)
                .collect(Collectors.toList());  // vrati kao listu
    }

    private List<Beer> listBeersByBeersNameAndStyle(String beerName, BeerStyle beerStyle) {

        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle);
    }

    private List<Beer> listBeersByStyle(BeerStyle beerStyle) {

        return beerRepository.findAllByBeerStyle(beerStyle);
    }

    List<Beer> listBeersByName(String beerName)  {

        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%");
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return Optional.ofNullable(beerMapper.beerToBeerDTO(beerRepository.findById(id).orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDTO(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerID, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerID).ifPresentOrElse(foundBeer -> {        //happy path, nađi po ID i ako ga nađeš update-aj ime, stil, upc, price te snimi u "bazu"
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setUpc(beer.getUpc());
            foundBeer.setPrice(beer.getPrice());
            // foundBeer.setQuantityOnHand(beer.setQuantityOnHand());
            // beerRepository.save(foundBeer);

            atomicReference.set(Optional.of(beerMapper.beerToBeerDTO(beerRepository.save(foundBeer))));

        }, () -> {
            atomicReference.set(Optional.empty());  // unhappy path
        } );

        return atomicReference.get();
    }

    @Override
    public Boolean deleteBeerById(UUID beerID) {

        if(beerRepository.existsById(beerID)){
            beerRepository.deleteById(beerID);
            return true;
        }

        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if (StringUtils.hasText(beer.getBeerName())){
                foundBeer.setBeerName(beer.getBeerName());
            }
            if (beer.getBeerStyle() != null){
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if (StringUtils.hasText(beer.getUpc())){
                foundBeer.setUpc(beer.getUpc());
            }
            if (beer.getPrice() != null){
                foundBeer.setPrice(beer.getPrice());
            }
            if (beer.getQuantityOnHand() != null){
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }
            atomicReference.set(Optional.of(beerMapper.beerToBeerDTO(beerRepository.save(foundBeer))));
        }, () -> {
            atomicReference.set(Optional.empty());
        });

        return atomicReference.get();
    }
}
