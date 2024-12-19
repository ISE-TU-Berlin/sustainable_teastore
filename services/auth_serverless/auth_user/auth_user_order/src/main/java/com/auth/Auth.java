package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.auth.entities.OrderItem;
import com.auth.entities.Order;
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

    @PostMapping(value = "/tools.descartes.teastore.auth/rest/useractions/placeorder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob,      
        @RequestParam("totalPriceInCents") long totalPriceInCents,
        @RequestParam("addressName") String addressName, @RequestParam("address1") String address1,
        @RequestParam("address2") String address2,
        @RequestParam("creditCardCompany") String creditCardCompany,
        @RequestParam("creditCardNumber") String creditCardNumber,
        @RequestParam("creditCardExpiryDate") String creditCardExpiryDate) {
          
      if (new ShaSecurityProvider().validate(blob) == null || blob.getOrderItems().isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
  
      blob.getOrder().setUserId(blob.getUID());
      blob.getOrder().setTotalPriceInCents(totalPriceInCents);
      blob.getOrder().setAddressName(addressName);
      blob.getOrder().setAddress1(address1);
      blob.getOrder().setAddress2(address2);
      blob.getOrder().setCreditCardCompany(creditCardCompany);
      blob.getOrder().setCreditCardExpiryDate(creditCardExpiryDate);
      blob.getOrder().setCreditCardNumber(creditCardNumber);
      blob.getOrder().setTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  
      long orderId;
      String persistence_address;
      try {
        String registry_address = "http://teastore-registry:8080/tools.descartes.teastore.registry";
        ResponseEntity<String[]> registry_response = restTemplate
                .getForEntity(registry_address + "/rest/services/tools.descartes.teastore.persistence", String[].class);
        String[] persistence_instances = registry_response.getBody();
        String random_instance = persistence_instances[(int)(Math.random() * (persistence_instances.length))];
  
        persistence_address = "http://"+random_instance+"/tools.descartes.teastore.persistence";
        ResponseEntity<Long> persistence_response = restTemplate
                .postForEntity(persistence_address + "/rest/orders", blob.getOrder(), Long.class);
        orderId = persistence_response.getBody();
      } catch (HttpClientErrorException.NotFound e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      } catch(Exception e) {
        return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
      }
      for (OrderItem item : blob.getOrderItems()) {
        try {
          item.setOrderId(orderId);
          ResponseEntity<Long> persistence_response = restTemplate.postForEntity(persistence_address + "/rest/orderitems", item, Long.class);
        } catch (HttpClientErrorException.NotFound e) {
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception e) {
          return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }
      }
      blob.setOrder(new Order());
      blob.getOrderItems().clear();
      blob = new ShaSecurityProvider().secure(blob);
      return new ResponseEntity<>(blob, HttpStatus.OK);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
