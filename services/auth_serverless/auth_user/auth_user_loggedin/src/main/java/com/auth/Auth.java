package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.auth.entities.message.SessionBlob;
import com.auth.security.ShaSecurityProvider;

import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Auth {

  @RestController
  class AuthController {
    RestTemplate restTemplate = new RestTemplate();

    @PostMapping(value = "/tools.descartes.teastore.auth/rest/useractions/isloggedin", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob) {
     return new ResponseEntity<>(new ShaSecurityProvider().validate(blob), HttpStatus.OK);
    }

  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
