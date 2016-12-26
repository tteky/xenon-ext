package com.tteky.xenonext.jaxrs;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public class EmployeePojo {
    @NotNull
    private String name;
    @Min(18)
    @Max(100)
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
