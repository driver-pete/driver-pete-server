package com.otognan.driverpete.logic.routes;


import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
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
    public List<String> routes(Principal principal, @RequestParam("isAtoB") boolean isAtoB) throws IOException, ParseException {
        User user = (User)((Authentication)principal).getPrincipal();
        List<byte[]> binaryRoutes = this.routesService.getBinaryRoutes(user, isAtoB);
        List<String> stringRoutes = new ArrayList<String>();
        for (byte[] binRoute: binaryRoutes) {
            
            stringRoutes.add(new String(Base64.encodeBase64(binRoute)));
        }
        return stringRoutes;
    }
    
    @RequestMapping(value = "/api/trajectory/routes/all", method = RequestMethod.DELETE)
    public void deleteAllRoutes(Principal principal) {
        User user = (User)((Authentication)principal).getPrincipal();
        this.routesService.deleteAllRoutes(user);
    }
}