package profchoper._security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;

import static profchoper._misc.Constant.PROF;
import static profchoper._misc.Constant.STUDENT;

@Configuration
public class ProfChoperSecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationSuccessHandler successHandler;

    @Autowired
    public ProfChoperSecurityConfig(@Qualifier("profChoperDataSource") DataSource dataSource,
                                    AccessDeniedHandler accessDeniedHandler,
                                    AuthenticationSuccessHandler successHandler) {

        this.dataSource = dataSource;
        this.accessDeniedHandler = accessDeniedHandler;
        this.successHandler = successHandler;
    }

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("SELECT username, password, enabled FROM users WHERE username = ?")
                .authoritiesByUsernameQuery("SELECT username, role FROM user_roles WHERE username = ?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/test").permitAll()
                .antMatchers("/prof/**").hasAnyRole(PROF)
                .antMatchers("/student/**").hasAnyRole(STUDENT)
                .anyRequest().authenticated()
                .and()
                .formLogin().successHandler(successHandler)
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler);
    }

}