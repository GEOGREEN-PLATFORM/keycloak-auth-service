package com.example.keycloak.auth.service.service;

import com.example.keycloak.auth.service.exception.CustomAccessDeniedException;
import com.example.keycloak.auth.service.mapper.UserMapper;
import com.example.keycloak.auth.service.model.dto.ListUsersResponse;
import com.example.keycloak.auth.service.model.dto.UserRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserEntity;
import com.example.keycloak.auth.service.repository.UserRepository;
import com.example.keycloak.auth.service.util.JwtParserUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.example.keycloak.auth.service.util.ExceptionStringUtil.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtParserUtil jwtParserUtil;

    public UserResponse updateUser(String token, String email, UserRequest userRequest) {
        if (!jwtParserUtil.extractEmailFromJwt(token).equals(email)) {
            throw new CustomAccessDeniedException("Недостаточно прав");
        }

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        if (userRequest.getBirthdate() != null) userEntity.setBirthdate(userRequest.getBirthdate());
        if (userRequest.getImage() != null) userEntity.setImage(userRequest.getImage());
        if (userRequest.getNumber() != null) userEntity.setNumber(userRequest.getNumber());
        if (userRequest.getFirstName() != null) userEntity.setFirstName(userRequest.getFirstName());
        if (userRequest.getLastName() != null) userEntity.setLastName(userRequest.getLastName());
        if (userRequest.getPatronymic() != null) userEntity.setPatronymic(userRequest.getPatronymic());

        return userMapper.toUserResponse(userRepository.saveAndFlush(userEntity));
    }

    public UserResponse getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return userMapper.toUserResponse(userEntity);
    }

    public ListUsersResponse getUsers(int page, int size, String search, String role, String status,
                                      LocalDate fromDate, LocalDate toDate) {
        Specification<UserEntity> spec = Specification.where(null);

        if (search != null && !search.isEmpty()) {
            String searchTerm = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), searchTerm),
                    cb.like(cb.lower(root.get("lastName")), searchTerm),
                    cb.like(cb.lower(root.get("patronymic")), searchTerm)
            ));
        }

        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }

        if (status != null) {
            boolean isEnabled = "active".equalsIgnoreCase(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), isEnabled));
        }

        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("creationDate").as(LocalDate.class), fromDate));
        }

        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("creationDate").as(LocalDate.class), toDate));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);

        ListUsersResponse response = new ListUsersResponse();
        response.setUsers(userPage.getContent().stream().map(userMapper::toUserResponse).toList());
        response.setCurrentPage(userPage.getNumber());
        response.setTotalItems(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());

        return response;
    }
}
