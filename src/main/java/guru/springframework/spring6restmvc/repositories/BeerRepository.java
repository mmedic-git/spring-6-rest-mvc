package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BeerRepository extends JpaRepository<Beer, UUID> {

        //implementiram filtriranje liste da špodržim query po beerName, Style i ostalo...


    // Milan NAPOMENA: kada JPA naleti na ovakav naziv metode findAllBy...IsLikeIgnoreCase
    // defacto će izgenerirati ovak select za bazu select ... from table t where upper(t.beerName) = upper('Some Name')

    // da želiš ručno kreirati query, ovako bi nekako
    // @Query("select * from beer u from Beer t where upper(t.beername) = ?1")


    //to tako radi jer smo naslijedili/extendali JpaRepository, koji nameće tu specifikaciju findAllBy________IsLikeIgnoreCase

    Page<Beer> findAllByBeerNameIsLikeIgnoreCase (String beerName, org.springframework.data.domain.Pageable pageable);

    Page<Beer> findAllByBeerStyle (BeerStyle beerStyle, org.springframework.data.domain.Pageable pageable);

    Page<Beer> findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle (String beerName, BeerStyle beerStyle, org.springframework.data.domain.Pageable pageable);


}
