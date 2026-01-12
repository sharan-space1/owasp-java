package customer.owasp_java;

import org.springframework.stereotype.Component;

import com.sap.cds.services.request.ModifiableUserInfo;
import com.sap.cds.services.request.UserInfo;
import com.sap.cds.services.runtime.UserInfoProvider;

@Component
public class UserInfoCustomizer implements UserInfoProvider {
    private UserInfoProvider defaultProvider;
    
    @Override
    public UserInfo get() {
        ModifiableUserInfo userInfo = UserInfo.create();
        if (defaultProvider != null) {
            UserInfo delegatedUserInfo = defaultProvider.get();
            if (delegatedUserInfo != null) {
                userInfo = delegatedUserInfo.copy();
                
                Object emailAttr = delegatedUserInfo.getAdditionalAttribute("email");
                if (emailAttr != null) {
                    String email = emailAttr.toString().trim();
                    if (!email.isEmpty()) {
                        userInfo.setName(email);
                    }
                }
            }
        }
        return userInfo;
    }
    
    @Override
    public void setPrevious(UserInfoProvider prev) {
        this.defaultProvider = prev;
    }
}