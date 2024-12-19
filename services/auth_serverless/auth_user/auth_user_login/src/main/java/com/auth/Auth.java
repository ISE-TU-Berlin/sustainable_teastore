package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.auth.entities.User;
import com.auth.security.BCryptProvider;
import com.auth.security.RandomSessionIdGenerator;
import com.auth.security.ShaSecurityProvider;
import com.auth.entities.message.SessionBlob;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.lang.Math;

@SpringBootApplication
public class Auth {

  @RestController
  class AuthController {
    RestTemplate restTemplate = new RestTemplate();

    @PostMapping(value = "/tools.descartes.teastore.auth/rest/useractions/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob, @RequestParam("name") String name, @RequestParam("password") String password) {
      User user;
      try {
        String registry_address = "http://teastore-registry:8080/tools.descartes.teastore.registry";
        ResponseEntity<String[]> registry_response = restTemplate
                .getForEntity(registry_address + "/rest/services/tools.descartes.teastore.persistence", String[].class);
        String[] persistence_instances = registry_response.getBody();
        String random_instance = persistence_instances[(int)(Math.random() * (persistence_instances.length))];
  
        String persistence_address = "http://"+random_instance+"/tools.descartes.teastore.persistence";
        ResponseEntity<User> persistence_response = restTemplate
                .getForEntity(persistence_address + "/rest/users/name/"+ name, User.class);
        user = persistence_response.getBody();
      } catch (HttpClientErrorException.NotFound e) {
        return new ResponseEntity<>(blob, HttpStatus.OK);
      } catch(Exception e) {
        return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
      }

      if (user != null && BCryptProvider.checkPassword(password, user.getPassword())
      ) {
        blob.setUID(user.getId());
        blob.setSID(new RandomSessionIdGenerator().getSessionId());
        blob = new ShaSecurityProvider().secure(blob);
        return new ResponseEntity<>(blob, HttpStatus.OK);
      }
      return new ResponseEntity<>(blob, HttpStatus.OK);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
