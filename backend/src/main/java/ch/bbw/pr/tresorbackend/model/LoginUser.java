package ch.bbw.pr.tresorbackend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * LoginUser
 *   DTO for simple login
 * @author Peter Rutschmann
 */
@Value
public class LoginUser {
   @NotBlank(message="E-Mail is required.")
   @Email(message="E-Mail must be valid.")
   private String email;

   @NotBlank(message="Password is required.")
   private String password;
}
