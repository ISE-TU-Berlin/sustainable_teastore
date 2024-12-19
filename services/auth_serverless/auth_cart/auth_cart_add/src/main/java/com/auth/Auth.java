package com.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.auth.entities.OrderItem;
import com.auth.entities.Product;
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

    @PostMapping(value = "/tools.descartes.teastore.auth/rest/cart/add/{pid}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SessionBlob> auth (@RequestBody SessionBlob blob, @PathVariable Long pid) {
      Product product;
      try {
        String registry_address = "http://teastore-registry:8080/tools.descartes.teastore.registry";
        ResponseEntity<String[]> registry_response = restTemplate
                .getForEntity(registry_address + "/rest/services/tools.descartes.teastore.persistence", String[].class);
        String[] persistence_instances = registry_response.getBody();
        String random_instance = persistence_instances[(int)(Math.random() * (persistence_instances.length))];
  
        String persistence_address = "http://"+random_instance+"/tools.descartes.teastore.persistence";
        ResponseEntity<Product> persistence_response = restTemplate
                .getForEntity(persistence_address + "/rest/products/"+ String.valueOf(pid), Product.class);
        product = persistence_response.getBody();
      } catch (HttpClientErrorException.NotFound e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      } catch(Exception e) {
        return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
      }

      for (OrderItem orderItem : blob.getOrderItems()) {
        if (orderItem.getProductId() == pid) {
          orderItem.setQuantity(orderItem.getQuantity() + 1);
          blob = new ShaSecurityProvider().secure(blob);
          return new ResponseEntity<>(blob, HttpStatus.OK);
        }
      }
      OrderItem item = new OrderItem();
      item.setProductId(pid);
      item.setQuantity(1);
      item.setUnitPriceInCents(product.getListPriceInCents());
      blob.getOrderItems().add(item);
      blob = new ShaSecurityProvider().secure(blob);
      return new ResponseEntity<>(blob, HttpStatus.OK);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Auth.class, args);
  }
}
