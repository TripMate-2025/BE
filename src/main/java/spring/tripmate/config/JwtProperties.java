package spring.tripmate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	@Value("${JWT_SECRET}")
    private String secret;

    @PostConstruct
    public void init() {
        System.out.println("✅ JwtProperties loaded! secret = " + secret);
        System.out.println("✅ SYSTEM ENV JWT_SECRET: " + System.getenv("JWT_SECRET"));
        System.out.println("✅ SYSTEM PROP JWT_SECRET: " + System.getProperty("JWT_SECRET"));
    }


    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { 
    	System.out.println("[DEBUG] JwtProperties.setSecret() called with: " + secret);
    	this.secret = secret; 
    }
}
