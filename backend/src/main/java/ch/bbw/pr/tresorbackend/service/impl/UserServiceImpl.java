package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.SafeDbCall;
import ch.bbw.pr.tresorbackend.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * UserServiceImpl
 *
 * @author Peter Rutschmann
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return SafeDbCall.safeDbCall(() -> userRepository.save(user), null);
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> user = SafeDbCall.safeDbCall(() -> userRepository.findById(userId), Optional.empty());
        return user.orElse(null);

    }

    @Override
    public User getUserByUuid(String userUuid) {
        Optional<User> user = SafeDbCall.safeDbCall(() -> userRepository.findByUserUuid(userUuid), Optional.empty());
        return user.orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        Optional<User> user = SafeDbCall.safeDbCall(() -> userRepository.findByEmail(email), Optional.empty());
        return user.orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return SafeDbCall.safeDbCall(() -> userRepository.findAll(), List.of());
    }

    @Override
    public User updateUser(User user) {
        Optional<User> optionalExistingUser = SafeDbCall.safeDbCall(() -> userRepository.findById(user.getId()), Optional.empty());
        if (optionalExistingUser.isEmpty()) return null;
        User existingUser = optionalExistingUser.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    public User updateUserByUuid(String userUuid, User user) {
        Optional<User> optionalExistingUser = SafeDbCall.safeDbCall(() -> userRepository.findByUserUuid(userUuid), Optional.empty());
        if (optionalExistingUser.isEmpty()) return null;
        User existingUser = optionalExistingUser.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    public boolean deleteUser(Long userId) {
        return SafeDbCall.safeDbCall(() -> userRepository.deleteById(userId));
    }

    @Override
    public boolean deleteUserByUuid(String userUuid) {
        User user = getUserByUuid(userUuid);
        if (user == null) return false;
        return SafeDbCall.safeDbCall(() -> userRepository.deleteById(user.getId()));
    }
}

