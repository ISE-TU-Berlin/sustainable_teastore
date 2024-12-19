package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import com.auth.entities.OrderItem;
import com.auth.security.ShaSecurityProvider;
import com.auth.entities.message.SessionBlob;

@SpringBootApplication
public class Auth {

  @RestController
  class AuthController {

    @PutMapping(value = "/tools.descartes.teastore.auth/rest/cart/{pid}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob, @PathVariable Long pid, @RequestParam("quantity") int quantity) {
      for (OrderItem item : blob.getOrderItems()) {
        if (item.getProductId() == pid) {
          item.setQuantity(quantity);
          blob = new ShaSecurityProvider().secure(blob);
          return new ResponseEntity<>(blob, HttpStatus.OK);
        }
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
