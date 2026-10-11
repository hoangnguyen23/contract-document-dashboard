package vn.contractgovernance.api.shared;

import java.util.Set;
import java.util.UUID;

public record Actor(UUID id, Set<String> roles, Set<UUID> branchIds, Set<String> permissions) {
    public Actor { roles = Set.copyOf(roles); branchIds = Set.copyOf(branchIds); permissions = Set.copyOf(permissions); }
    public boolean hasRole(String role) { return roles.contains(role); }
    public boolean hasAnyRole(String... candidates) { return java.util.Arrays.stream(candidates).anyMatch(roles::contains); }
    public boolean hasPermission(String permission) { return permissions.contains(permission); }
    public boolean inBranch(UUID branchId) { return branchId != null && branchIds.contains(branchId); }
    public void requireRole(String... candidates) { if (!hasAnyRole(candidates)) throw ApiException.of("ROLE_FORBIDDEN", "Bạn không có quyền thực hiện thao tác này."); }
    public void requireBranch(UUID branchId) { if (!inBranch(branchId)) throw ApiException.of("NOT_FOUND", "Không tìm thấy tài nguyên."); }
}
