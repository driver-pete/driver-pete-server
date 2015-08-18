package com.otognan.driverpete.logic.routes;


import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.otognan.driverpete.security.User;


@RestController
public class RoutesController {
    
    @Autowired
    private RoutesService routesService;
    
    @RequestMapping(value = "/api/trajectory/routes", method = RequestMethod.GET)
    public List<byte[]> routes(Principal principal, @RequestParam("isAtoB") boolean isAtoB) throws IOException, ParseException {
        User user = (User)((Authentication)principal).getPrincipal();
        List<byte[]> binaryRoutes = this.routesService.getBinaryRoutes(user, isAtoB);
        for (int i = 0; i < binaryRoutes.size(); i++) {
            binaryRoutes.set(i, Base64.encodeBase64(binaryRoutes.get(i)));
        }
        return binaryRoutes;
    }
}