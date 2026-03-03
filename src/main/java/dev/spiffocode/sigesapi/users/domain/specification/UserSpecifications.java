package dev.spiffocode.sigesapi.users.domain.specification;

import dev.spiffocode.sigesapi.users.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.users.application.service.UserTypeFilter;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    @SuppressWarnings("unchecked")
    public static Specification<@NonNull User> searchQuery(String query) {
        if (query == null || query.isBlank()) return null;
        return (root, cq, cb) -> {
            String pattern = "%" + query.toLowerCase() + "%";

            Join<User, Student> studentJoin = (Join<User, Student>) root.getJoins().stream()
                    .filter(j -> j.getJavaType().equals(Student.class))
                    .findFirst()
                    .orElseGet(() -> root.join(Student.class.getSimpleName(), JoinType.LEFT));

            Join<User, InstitutionalStaff> staffJoin = (Join<User, InstitutionalStaff>) root.getJoins().stream()
                    .filter(j -> j.getJavaType().equals(InstitutionalStaff.class))
                    .findFirst()
                    .orElseGet(() -> root.join(InstitutionalStaff.class.getSimpleName(), JoinType.LEFT));

            return cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phoneNumber")), pattern),
                cb.like(cb.lower(studentJoin.get("registrationNumber")), pattern),
                cb.like(cb.lower(staffJoin.get("employeeNumber")), pattern)
            );
        };
    }

    public static Specification<@NonNull User> userTypeIn(List<UserTypeFilter> types) {
        if (types == null || types.isEmpty()) return null;
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (types.contains(UserTypeFilter.ADMIN))
                predicates.add(cb.equal(root.type(), Admin.class));
            if (types.contains(UserTypeFilter.STUDENT))
                predicates.add(cb.equal(root.type(), Student.class));
            if (types.contains(UserTypeFilter.INSTITUTIONAL_STAFF))
                predicates.add(cb.equal(root.type(), InstitutionalStaff.class));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<@NonNull User> byShowMode(ShowModeFilter showMode) {
        if (showMode == null || showMode == ShowModeFilter.ALL) return null;
        return (root, cq, cb) -> showMode == ShowModeFilter.ACTIVE
                ? cb.isNull(root.get("deletedAt"))
                : cb.isNotNull(root.get("deletedAt"));
    }
}