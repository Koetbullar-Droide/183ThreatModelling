package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SecretController
 *
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {

   private SecretService secretService;
   private UserService userService;

   // create secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null) return ResponseEntity.notFound().build();

      //transfer secret and encrypt content
      Secret secret = new Secret(null, user.getId(), new EncryptUtil(newSecret.getEncryptPassword()).encrypt(newSecret.getContent().toString()));
      //save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.createSecret, secret saved in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret saved");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.createSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Get Secrets by userUuid REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byuseruuid")
   public ResponseEntity<List<Secret>> getSecretsByUserUuid(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByUserUuid " + credentials);

      User user = userService.getUserByUuid(credentials.getUserUuid());
      if (user == null) return ResponseEntity.notFound().build();

      List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserUuid secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for (Secret secret : secrets) {
         try {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword()).decrypt(secret.getContent()));
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByUserUuid " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
      }

      System.out.println("SecretController.getSecretsByUserUuid " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get Secrets by email REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<List<Secret>> getSecretsByEmail(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByEmail " + credentials);

      User user = userService.findByEmail(credentials.getEmail());
      if (user == null) return ResponseEntity.notFound().build();

      List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for (Secret secret : secrets) {
         try {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword()).decrypt(secret.getContent()));
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
      }

      System.out.println("SecretController.getSecretsByEmail " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get All Secrets REST API
   // http://localhost:8080/api/secrets
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<Secret>> getAllSecrets() {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
   }

   // Build Update Secrete REST API
   // http://localhost:8080/api/secrets/{secretUuid}
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{secretUuid}")
   public ResponseEntity<String> updateSecret(@PathVariable("secretUuid") String secretUuid, @Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.updateSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }

      //get Secret with uuid
      Secret dbSecrete = secretService.getSecretByUuid(secretUuid);
      if (dbSecrete == null) {
         System.out.println("SecretController.updateSecret, secret not found in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret not found in db");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null) return ResponseEntity.notFound().build();

      //check if Secret in db has not same userid
      if (!Objects.equals(dbSecrete.getUserId(), user.getId())) {
         System.out.println("SecretController.updateSecret, not same user id");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret has not same user id");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(json);
      }
      //check if Secret can be decrypted with password
      try {
         new EncryptUtil(newSecret.getEncryptPassword()).decrypt(dbSecrete.getContent());
      } catch (EncryptionOperationNotPossibleException e) {
         System.out.println("SecretController.updateSecret, invalid password");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Password not correct.");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      //modify Secret in db.
      Secret secret = new Secret(dbSecrete.getId(), user.getId(), new EncryptUtil(newSecret.getEncryptPassword()).encrypt(newSecret.getContent().toString()));
      Secret updatedSecret = secretService.updateSecret(secret);
      if (updatedSecret == null) {
         return ResponseEntity.notFound().build();
      }
      System.out.println("SecretController.updateSecret, secret updated in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret updated");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.updateSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Delete Secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{secretUuid}")
   public ResponseEntity<String> deleteSecret(@PathVariable("secretUuid") String secretUuid, @RequestBody EncryptCredentials credentials) {
      Secret dbSecret = secretService.getSecretByUuid(secretUuid);
      if (dbSecret == null) return ResponseEntity.notFound().build();

      User user = userService.findByEmail(credentials.getEmail());
      if (user == null) return ResponseEntity.notFound().build();
      if (!Objects.equals(dbSecret.getUserId(), user.getId())) {
         System.out.println("SecretController.deleteSecret, not same user id");
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Secret has not same user id");
      }

      try {
         new EncryptUtil(credentials.getEncryptPassword()).decrypt(dbSecret.getContent());
      } catch (EncryptionOperationNotPossibleException e) {
         System.out.println("SecretController.deleteSecret, invalid password");
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password not correct.");
      }

      secretService.deleteSecretByUuid(secretUuid);
      System.out.println("SecretController.deleteSecret succesfully: " + secretUuid);
      return new ResponseEntity<>("Secret successfully deleted!", HttpStatus.OK);
   }
}
