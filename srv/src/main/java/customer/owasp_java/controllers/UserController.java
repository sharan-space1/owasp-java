package customer.owasp_java.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cds.services.request.UserInfo;

@RestController
@RequestMapping("/rest")
public class UserController {

    private final UserInfo userInfo;

    public UserController(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoAmI() {

        Map<String, Object> response = new HashMap<>();

        response.put("username", userInfo.getName());
        response.put("roles", userInfo.getRoles());
        response.put("email", userInfo.getAttributeValues("email"));
        response.put("given_name", userInfo.getAttributeValues("given_name"));
        response.put("family_name", userInfo.getAttributeValues("family_name"));

        return response;
    }

    @GetMapping("/confidentialinfo")
    public Map<String, Object> confidentialInfo() {
        
        Map<String, Object> response = new HashMap<>();
        
        if(userInfo.hasRole("admin")) {
            response.put("id", userInfo.getId());
            response.put("tenant", userInfo.getTenant());
        } else {
            response.put("error", "Loggedin user is not an admin !!");
        }

        return response;
    }
}