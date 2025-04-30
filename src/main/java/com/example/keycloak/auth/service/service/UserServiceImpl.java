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
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.ADMIN;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.OPERATOR;
import static com.example.keycloak.auth.service.util.ExceptionStringUtil.FORBIDDEN;
import static com.example.keycloak.auth.service.util.ExceptionStringUtil.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtParserUtil jwtParserUtil;

    public UserResponse updateUser(String token, String email, UserRequest userRequest) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        if (!jwtParserUtil.extractEmailFromJwt(token).equals(email)) {
            if (!(ADMIN.equals(jwtParserUtil.extractRoleFromJwt(token))
                    && OPERATOR.equals(userEntity.getRole()))) {
                throw new CustomAccessDeniedException(FORBIDDEN);
            }
        }

        if (userRequest.getBirthdate() != null) userEntity.setBirthdate(userRequest.getBirthdate());
        if (userRequest.getImage() != null) userEntity.setImage(userRequest.getImage());
        if (userRequest.getNumber() != null) userEntity.setNumber(userRequest.getNumber());
        if (userRequest.getFirstName() != null) userEntity.setFirstName(userRequest.getFirstName());
        if (userRequest.getLastName() != null) userEntity.setLastName(userRequest.getLastName());
        if (userRequest.getPatronymic() != null) userEntity.setPatronymic(userRequest.getPatronymic());

        return userMapper.toUserResponse(userRepository.saveAndFlush(userEntity));
    }

    public UserResponse getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return userMapper.toUserResponse(userEntity);
    }

    public UserResponse getUserById(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return userMapper.toUserResponse(userEntity);
    }

    public ListUsersResponse getUsers(int page, int size, String search, String role, String status,
                                      LocalDate fromDate, LocalDate toDate) {
        Specification<UserEntity> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            String[] parts = search.trim().toLowerCase().split("\\s+");
            spec = spec.and((root, query, cb) -> {
                Expression<String> lastName = cb.lower(root.get("lastName"));
                Expression<String> firstName = cb.lower(root.get("firstName"));
                Expression<String> patronymic = cb.lower(root.get("patronymic"));

                switch (Math.min(parts.length, 3)) {
                    case 1:
                        String term = "%" + parts[0] + "%";
                        return cb.or(
                                cb.like(lastName, term),
                                cb.like(firstName, term),
                                cb.like(patronymic, term)
                        );
                    case 2:
                        String term0 = "%" + parts[0] + "%";
                        String term1 = "%" + parts[1] + "%";

                        Predicate byLastFirst = cb.and(
                                cb.like(lastName, term0),
                                cb.like(firstName, term1)
                        );
                        Predicate byLastPatronymic = cb.and(
                                cb.like(lastName, term0),
                                cb.like(patronymic, term1)
                        );
                        Predicate byFirstPatronymic = cb.and(
                                cb.like(firstName, term0),
                                cb.like(patronymic, term1)
                        );
                        return cb.or(byLastFirst, byLastPatronymic, byFirstPatronymic);
                    case 3:
                    default:
                        return cb.and(
                                cb.like(lastName, "%" + parts[0] + "%"),
                                cb.like(firstName, "%" + parts[1] + "%"),
                                cb.like(patronymic, "%" + parts[2] + "%")
                        );
                }
            });
        }

        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), role)
            );
        } else {
            spec = spec.and((root, query, cb) ->
                    root.get("role").in("admin", "operator")
            );
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
