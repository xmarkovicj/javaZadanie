package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Primary
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogRepository activityLogRepository;

    // používaš v AuthController.register(...)
    public User register(User user) {
        // findByEmail vracia Optional<User>, takže treba isPresent()
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User with email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ak niekde ručne vytváraš usera
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());

            // heslo zmeniť len keď je niečo zadané
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            return userRepository.save(existingUser);
        });
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            // najprv zmažeme logy, aby nepadali FK constrainty
            activityLogRepository.deleteByUser_Id(id);
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        return user;
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Tu môžeš pridať role/authority, ak ich máš
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities("USER")
                .build();
    }
}
