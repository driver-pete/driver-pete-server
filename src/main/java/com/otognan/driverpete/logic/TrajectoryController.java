package com.otognan.driverpete.logic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TrajectoryController {

    @RequestMapping(value = "/api/trajectory/hello", method = RequestMethod.GET)
    public String getCurrent() {
        return "HELLO";
    }
}