package ch.bbw.pr.tresorbackend.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/**
 * EncryptCredentials
 *
 * @author Peter Rutschmann
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EncryptCredentials {
   private String userUuid;
   private String email;
   @NotEmpty(message = "encryption password id is required.")
   private String encryptPassword;
}
