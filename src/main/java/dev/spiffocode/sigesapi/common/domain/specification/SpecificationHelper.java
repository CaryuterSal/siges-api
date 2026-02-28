package dev.spiffocode.sigesapi.common.domain.specification;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationHelper {

    public static <T> Specification<@NonNull T> cast(Specification<? super T> spec) {
        return (root, query, cb) -> {

            @SuppressWarnings("unchecked")
            Specification<@NonNull T> sp = (Specification<@NonNull T>) spec;
            return sp.toPredicate(root, query, cb);
        };
    }
}
