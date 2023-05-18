package guru.springframework.spring6restmvc.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// ovaj file nam zapravo više ne treba, jer smo sav exception handling prebacili u NotFoundException klasu. Zbog toga smo ovdje i iskomentirali   @ControllerAdvice  i  @ExceptionHandler
// anotacije, tako da se ovaj dolje kod nikada neće izvšiti zapravo.

// refactoring dižemo exception handling u NotFoundException klasu
// @ControllerAdvice                                  // uz @ControllerAdvice  anotaciju ova klasa će postati globalni exception handler za cijelu aplikaciju
public class ExceptionController {

    // također zbog refactoringa u NotFoundException klasu smo iskomentirali
    // @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException() {

        System.out.println("In exception handler");

        return ResponseEntity.notFound().build();  //ovim će nam test getBeerByIdNotFound() završit uspješno iz razloga što smo shendlali NotFoundException

    }
}
