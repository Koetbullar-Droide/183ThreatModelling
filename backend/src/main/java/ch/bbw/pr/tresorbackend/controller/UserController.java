package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptService;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserController
 *
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private PasswordEncryptService passwordService;

    // build create User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser, BindingResult bindingResult) {
        //captcha
        //todo add implementation

        logger.info("UserController.createUser: captcha passed.");

        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.toList());
            logger.info("UserController.createUser validation errors: {}", errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            logger.info("UserController.createUser, validation fails");
            return ResponseEntity.badRequest().body(json);
        }
        logger.info("UserController.createUser: input validation passed");

        //password validation
        if (!registerUser.getPassword().equals(registerUser.getPasswordConfirmation())) {
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "Password and password confirmation do not match");
         String json = new Gson().toJson(obj);
         logger.info("UserController.createUser, password confirmation mismatch");
        return ResponseEntity.badRequest().body(json);
      }
      logger.info("UserController.createUser, password validation passed");

        //transform registerUser to user
        User user = new User(null, registerUser.getFirstName(), registerUser.getLastName(), registerUser.getEmail(), passwordService.hashPassword(registerUser.getPassword()));

        User savedUser = userService.createUser(user);
        JsonObject obj = new JsonObject();
        if (savedUser != null) {
            logger.info("UserController.createUser, user saved in db");
            obj.addProperty("answer", "User saved");
        } else {
            logger.info("UserController.createUser, user not saved in db");
            obj.addProperty("answer", "User not saved");
        }
        String json = new Gson().toJson(obj);
        logger.info("UserController.createUser completed");
        return ResponseEntity.accepted().body(json);
    }

    // build get user by uuid REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{userUuid}")
    public ResponseEntity<User> getUserById(@PathVariable("userUuid") String userUuid) {
        User user = userService.getUserByUuid(userUuid);
        if (user == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // Build Get All Users REST API
    // http://localhost:8080/api/users
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Build Update User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{userUuid}")
    public ResponseEntity<User> updateUser(@PathVariable("userUuid") String userUuid, @RequestBody User user) {
        User updatedUser = userService.updateUserByUuid(userUuid, user);
        if (updatedUser == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // Build Delete User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{userUuid}")
    public ResponseEntity<String> deleteUser(@PathVariable("userUuid") String userUuid) {
        if (userService.deleteUserByUuid(userUuid))
            return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
        return ResponseEntity.notFound().build();
    }

    // get user uuid by email
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/byemail")
    public ResponseEntity<String> getUserUuidByEmail(@RequestBody EmailAdress email, BindingResult bindingResult) {
        System.out.println("UserController.getUserUuidByEmail: " + email);
        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.toList());
            System.out.println("UserController.createUser " + errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            logger.info("UserController.getUserIdByEmail validation fails");
            return ResponseEntity.badRequest().body(json);
        }

        logger.info("UserController.getUserUuidByEmail: input validation passed");

        User user = userService.findByEmail(email.getEmail());
        if (user == null) {
            logger.info("UserController.getUserUuidByEmail, no user found for email");
            JsonObject obj = new JsonObject();
            obj.addProperty("message", "No user found with this email");
            String json = new Gson().toJson(obj);

            logger.info("UserController.getUserUuidByEmail, fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }
        logger.info("UserController.getUserUuidByEmail, user find by email");
        JsonObject obj = new JsonObject();
        obj.addProperty("answer", user.getUserUuid());
        String json = new Gson().toJson(obj);
        logger.info("UserController.getUserUuidByEmail " + json);
        return ResponseEntity.accepted().body(json);
    }

    // simple login with no websecurity, just email and password
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> doLoginUser(@Valid @RequestBody LoginUser loginUser, BindingResult bindingResult) {
        logger.info("UserController.doLoginUser called");

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(new LoginResponse(errorMessage, null));
        }

        User user = userService.findByEmail(loginUser.getEmail());
        if (user == null) {
            logger.info("UserController.doLoginUser: user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
               .body(new LoginResponse("Invalid email or password", null));
        }

      if (!loginUser.getPassword().equals(user.getPassword())) {
         System.out.println("UserController.doLoginUser: invalid password");
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
               .body(new LoginResponse("Invalid email or password", null));
      }

        logger.info("UserController.doLoginUser: login successful");
        return ResponseEntity.ok(new LoginResponse("Login successful", user.getUserUuid()));
    }

}

