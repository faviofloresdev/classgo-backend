package com.classgo.backend.infrastructure.security;

import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.shared.exception.UnauthorizedOperationException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new UnauthorizedOperationException("Authentication required");
        }
        return authUser;
    }

    public static UUID currentUserId() {
        return currentUser().userId();
    }

    public static void requireRole(UserRole role) {
        if (currentUser().role() != role) {
            throw new UnauthorizedOperationException("Access denied for role " + currentUser().role());
        }
    }
}
