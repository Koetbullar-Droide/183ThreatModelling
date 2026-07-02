package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecretControllerTest {

   private final SecretService secretService = mock(SecretService.class);
   private final UserService userService = mock(UserService.class);
   private final SecretController secretController = new SecretController(secretService, userService);
   private final ObjectMapper objectMapper = new ObjectMapper();

   @Test
   void getAllSecretsIsForbidden() {
      ResponseEntity<List<Secret>> response = secretController.getAllSecrets();

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      verify(secretService, never()).getAllSecrets();
   }

   @Test
   void secretJsonContainsUuidButNoInternalIds() throws Exception {
      Secret secret = new Secret(3L, 1L, "{\"kind\":\"note\"}");
      secret.setSecretUuid("cccccccc-cccc-4ccc-8ccc-cccccccccccc");

      String json = objectMapper.writeValueAsString(secret);

      assertTrue(json.contains("secretUuid"));
      assertFalse(json.contains("\"id\""));
      assertFalse(json.contains("userId"));
   }

   @Test
   void getSecretsByUserUuidUsesInternalUserIdOnlyAfterUuidLookup() {
      EncryptCredentials credentials = new EncryptCredentials("11111111-1111-4111-8111-111111111111", null, "xxxyyy");
      User user = new User(1L, "Hans", "Muster", "hans.muster@bbw.ch", "hashedPassword");
      user.setUserUuid(credentials.getUserUuid());
      when(userService.getUserByUuid(credentials.getUserUuid())).thenReturn(user);
      when(secretService.getSecretsByUserId(user.getId())).thenReturn(List.of());

      ResponseEntity<List<Secret>> response = secretController.getSecretsByUserUuid(credentials.getUserUuid(), credentials);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      verify(secretService).getSecretsByUserId(1L);
   }

@Test
    void updateSecretRejectsForeignSecret() throws Exception {
       NewSecret newSecret = new NewSecret("hans.muster@bbw.ch", objectMapper.readTree("{\"kind\":\"note\",\"content\":\"new\"}"), "xxxyyy");
       Secret foreignSecret = new Secret(3L, 2L, "encryptedContent");
       foreignSecret.setSecretUuid("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
       when(secretService.getSecretByUuid(foreignSecret.getSecretUuid())).thenReturn(foreignSecret);
       User requestingUser = new User(1L, "Hans", "Muster", "hans.muster@bbw.ch", "hashedPassword");
       String requestingUserUuid = "11111111-1111-4111-8111-111111111111";
       requestingUser.setUserUuid(requestingUserUuid);
       when(userService.getUserByUuid(requestingUserUuid)).thenReturn(requestingUser);

       ResponseEntity<String> response = secretController.updateSecret(foreignSecret.getSecretUuid(), requestingUserUuid, newSecret, bindingResult(newSecret));

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      verify(secretService, never()).updateSecret(any(Secret.class));
   }

@Test
    void deleteSecretRejectsForeignSecret() {
       EncryptCredentials credentials = new EncryptCredentials(null, "hans.muster@bbw.ch", "xxxyyy");
       Secret foreignSecret = new Secret(3L, 2L, "encryptedContent");
       foreignSecret.setSecretUuid("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
       when(secretService.getSecretByUuid(foreignSecret.getSecretUuid())).thenReturn(foreignSecret);
       User requestingUser = new User(1L, "Hans", "Muster", "hans.muster@bbw.ch", "hashedPassword");
       String requestingUserUuid = "11111111-1111-4111-8111-111111111111";
       requestingUser.setUserUuid(requestingUserUuid);
       when(userService.getUserByUuid(requestingUserUuid)).thenReturn(requestingUser);

       ResponseEntity<String> response = secretController.deleteSecret(foreignSecret.getSecretUuid(), requestingUserUuid, credentials);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      verify(secretService, never()).deleteSecretByUuid(foreignSecret.getSecretUuid());
   }

@Test
    void deleteSecretDeletesOwnedSecretWithCorrectPassword() {
       EncryptCredentials credentials = new EncryptCredentials(null, "hans.muster@bbw.ch", "xxxyyy");
       String encryptedContent = new EncryptUtil(credentials.getEncryptPassword()).encrypt("{\"kind\":\"note\"}");
       Secret ownedSecret = new Secret(3L, 1L, encryptedContent);
       ownedSecret.setSecretUuid("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
       when(secretService.getSecretByUuid(ownedSecret.getSecretUuid())).thenReturn(ownedSecret);
       User requestingUser = new User(1L, "Hans", "Muster", "hans.muster@bbw.ch", "hashedPassword");
       String requestingUserUuid = "11111111-1111-4111-8111-111111111111";
       requestingUser.setUserUuid(requestingUserUuid);
       when(userService.getUserByUuid(requestingUserUuid)).thenReturn(requestingUser);

       ResponseEntity<String> response = secretController.deleteSecret(ownedSecret.getSecretUuid(), requestingUserUuid, credentials);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(secretService).deleteSecretByUuid(ownedSecret.getSecretUuid());
   }

   private BindingResult bindingResult(NewSecret newSecret) {
      return new BeanPropertyBindingResult(newSecret, "newSecret");
   }
}

