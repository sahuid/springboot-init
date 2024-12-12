package com.sahuid.springbootinit.model.enums;

import lombok.Data;

public enum UserRoleEnums {
    USER(0, "user"),
    ADMIN(1, "admin");
    ;

    private int userRole;

    private String roleName;

    UserRoleEnums(int userRole, String roleName) {
        this.userRole = userRole;
        this.roleName = roleName;
    }


    public static String getCurrentRoleName(int userRole) {
        for (UserRoleEnums enums : values()) {
            if (enums.getUserRole() == userRole){
                return enums.getRoleName();
            }
        }
        return "";
    }

    public static UserRoleEnums getCurrentUserRoleEnum(String roleName) {
        for (UserRoleEnums enums : values()) {
            if (enums.getRoleName().equals(roleName)) {
                return enums;
            }
        }
        return null;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
