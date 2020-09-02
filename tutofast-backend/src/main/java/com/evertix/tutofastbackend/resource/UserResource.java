package com.evertix.tutofastbackend.resource;

import com.evertix.tutofastbackend.model.Rol;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserResource {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String lastName;
    private Date birthday;
    private String email;
    private String phone;
    private String address;
    private int totalStar;
    private Boolean active;
    private String linkedln;
    private Rol role;
}
