package dev.spiffocode.sigesapi.users.model;

import java.util.Arrays;

import lombok.*;
import org.apache.catalina.Manager;

/**
 * The roles a {@link User} can acquire in the system
 */
@Getter
@ToString
@AllArgsConstructor
public enum RoleAuthority {
    ADMIN("ROLE_ADMIN", Admin.class),
    MANAGER("ROLE_INSTITUTIONAL_STAFF", InstitutionalStaff.class),
    CLIENT("ROLE_STUDENT", Student.class);

    private final String authority;
    private final Class<? extends User> userClazz;

    /**
     * factory that takes the subclass of a {@link User} as param
     * @param clazz a subclass of the user
     * @return a {@link RoleAuthority}
     */
    public static RoleAuthority fromClazz(Class<? extends User> clazz){
        return Arrays.stream(values())
                .filter(role -> role.getUserClazz().equals(clazz))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalArgumentException("Could not find role authority from clazz: " + clazz.getName())
                );
    }

    /**
     * Get the authority String for spring security without the {@code ROLE_} prefix
     * @return Role without the prefix
     */
    public @NonNull String getRole(){
        return getAuthority().replace("ROLE_", "");
    }
}
