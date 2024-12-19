package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.auth.entities.message.SessionBlob;
import com.auth.entities.Order;

@SpringBootApplication
public class Auth {

  @RestController
  class AuthController {

    @PostMapping(value = "/tools.descartes.teastore.auth/rest/useractions/logout", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob) {
      blob.setUID(null);
      blob.setSID(null);
      blob.setOrder(new Order());
      blob.getOrderItems().clear();
      return new ResponseEntity<>(blob, HttpStatus.OK);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
