package com.storeops.staff;

public class AuthResponse {

    private final String token;
    private final String staffId;
    private final String role;

    public AuthResponse(String token, String staffId, String role) {
        this.token = token;
        this.staffId = staffId;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getRole() {
        return role;
    }
}
