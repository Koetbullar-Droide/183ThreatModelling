package ch.bbw.pr.tresorbackend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * PasswordEncryptService
 *   used to hash password and verify match
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptService {
   private final BCryptPasswordEncoder passwordEncoder;

   public PasswordEncryptService() {
      this.passwordEncoder = new BCryptPasswordEncoder();
   }

   public String hashPassword(String password) {
      return passwordEncoder.encode(password);
   }

   public boolean matches(String password, String hashedPassword) {
      if (password == null || hashedPassword == null) {
         return false;
      }
      return passwordEncoder.matches(password, hashedPassword);
   }
}
