package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Primary //obzirom da je ovo 2. implementacija iste klase BeerService, jedna mora biti @Primary, obzirom da su obje na Classpath-u. Na ovaj način ova klasa dobiva prednost nad onom
         // iz BeerServiceImpl implementacije . Ta originalna klasa radi sa H2 inmemory i sadrži 3 objekta, ručno napravljena, a ova radi sa mysql bazom podataka
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;


    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {

        Page<Beer> beerPage;


        //uvodimo pageing, tako da već ograničimo listu u startu. U
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if (StringUtils.hasText(beerName) && beerStyle == null) {

            beerPage = listBeersByName(beerName, pageRequest);
        }

        else if (!StringUtils.hasText(beerName) && (beerStyle != null)) {
            beerPage = listBeersByStyle(beerStyle, pageRequest);
        }

        else if (StringUtils.hasText(beerName) && (beerStyle != null)) {

            beerPage = listBeersByBeersNameAndStyle (beerName, beerStyle, pageRequest);

            }

        else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory) {

            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }

        return beerPage.map(beerMapper::beerToBeerDTO);

        // return beerRepository
        /* promijenili smo logiku da više metoda ne vraća List<>, nego Page<>

        return beerList.stream()
                .map(beerMapper::beerToBeerDTO)
                .collect(Collectors.toList());  // vrati kao listu

         */
    }

    //sve public metode su testable
    public PageRequest buildPageRequest (Integer pageNumber, Integer pageSize) {

        int queryPageNUmber;
        int queryPageSize;

        if (pageNumber != null && pageNumber > 0 ) {

            queryPageNUmber = pageNumber -1;

        } else {
            queryPageNUmber = DEFAULT_PAGE;
        }

        if (pageSize == null) {
            queryPageSize = DEFAULT_PAGE_SIZE;
        } else {
            if (pageSize > 1000)  {
                queryPageSize = 1000;
            } else {
                queryPageSize = pageSize;
            }
        }

        return PageRequest.of(queryPageNUmber, queryPageSize);
    }

    private Page<Beer> listBeersByBeersNameAndStyle(String beerName, BeerStyle beerStyle, org.springframework.data.domain.Pageable pageable) {

        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, (org.springframework.data.domain.Pageable) pageable);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, org.springframework.data.domain.Pageable pageable) {

        return beerRepository.findAllByBeerStyle(beerStyle, (org.springframework.data.domain.Pageable) pageable);
    }

    Page<Beer> listBeersByName(String beerName, org.springframework.data.domain.Pageable pageable)  {

        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", (org.springframework.data.domain.Pageable) pageable);
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
