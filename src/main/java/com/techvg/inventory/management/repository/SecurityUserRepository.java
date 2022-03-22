package com.techvg.inventory.management.repository;

import com.techvg.inventory.management.domain.SecurityUser;
import com.techvg.inventory.management.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the SecurityUser entity.
 */
@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, Long>, JpaSpecificationExecutor<SecurityUser> {
    @Query(
        value = "select distinct securityUser from SecurityUser securityUser left join fetch securityUser.securityPermissions left join fetch securityUser.securityRoles",
        countQuery = "select count(distinct securityUser) from SecurityUser securityUser"
    )
    Page<SecurityUser> findAllWithEagerRelationships(Pageable pageable);

    @Query(
        "select distinct securityUser from SecurityUser securityUser left join fetch securityUser.securityPermissions left join fetch securityUser.securityRoles"
    )
    List<SecurityUser> findAllWithEagerRelationships();

    @Query(
        "select securityUser from SecurityUser securityUser left join fetch securityUser.securityPermissions left join fetch securityUser.securityRoles where securityUser.id =:id"
    )
    Optional<SecurityUser> findOneWithEagerRelationships(@Param("id") Long id);

    Optional<SecurityUser> findOneByActivationKey(String activationKey);

    List<SecurityUser> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndLastModifiedBefore(Instant dateTime);

    Optional<SecurityUser> findOneByResetKey(String resetKey);

    Optional<SecurityUser> findOneByEmailIgnoreCase(String email);

    Optional<SecurityUser> findOneByLogin(String login);

    @EntityGraph(attributePaths = "securityRoles")
    @Query(
        value = "select l from SecurityUser l " + "join l.securityRoles sr " + "join sr.securityPermissions sp " + "where l.login = :login"
    )
    Optional<SecurityUser> findOneWithSecurityRolesByLogin(@Param("login") String login);

    @EntityGraph(attributePaths = "securityRoles")
    Optional<SecurityUser> findOneWithSecurityRolesByEmailIgnoreCase(String email);

    Page<SecurityUser> findAllByLoginNot(Pageable pageable, String login);

    Optional<SecurityUser> findOneWithSecurityPermissionsByLogin(String name);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);
}
