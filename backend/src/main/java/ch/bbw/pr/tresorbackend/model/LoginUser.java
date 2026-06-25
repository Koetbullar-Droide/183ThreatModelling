package ch.bbw.pr.tresorbackend.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.ToString;
import lombok.Value;

/**
 * LoginUser
 *   DTO for simple login
 * @author Peter Rutschmann
 */
@Value
public class LoginUser {
   @NotEmpty (message="E-Mail is required.")
   private String email;

   @NotEmpty (message="Password is required.")
   @ToString.Exclude
   private String password;
}