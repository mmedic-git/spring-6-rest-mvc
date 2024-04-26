package guru.springframework.spring6restmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        //by default, Spring security zahtijeva CRSF flag, moramo definirati da ga za /api pozive ne želimo (dakle za REST pozive csrf je diseblan, a za web i dalje može biti zahtjevan

        http.authorizeHttpRequests()
                .anyRequest().authenticated()
                .and().httpBasic(Customizer.withDefaults())
                .csrf().ignoringRequestMatchers("/api/**");  //ignoriraj sve što počinje sa /api

        return http.build();
    }

}
