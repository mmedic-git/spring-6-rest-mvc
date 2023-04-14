package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.Beer;

import java.util.UUID;

public interface BeerService {

    Beer getBeerById(UUID id);
}
