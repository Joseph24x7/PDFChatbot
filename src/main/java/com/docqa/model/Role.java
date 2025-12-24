package com.docqa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {

    user("user"),
    assistant("assistant");

    private final String roleName;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Role role : Role.values()) {
            if (role.roleName.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

    @JsonValue
    public String getRoleName() {
        return roleName;
    }
}
