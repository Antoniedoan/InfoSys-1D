package profchoper.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

import static profchoper._misc.Constant.ROLE_PROF;
import static profchoper._misc.Constant.ROLE_STUDENT;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ProfChoperAuthSuccessHandler successHandler;

    @Autowired
    @Qualifier("profChoperDataSource")
    private DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("SELECT email, password FROM users WHERE email = ?")
                .authoritiesByUsernameQuery("SELECT email, role FROM users WHERE email = ?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/student").hasRole(ROLE_STUDENT)
                .antMatchers("/prof").hasRole(ROLE_PROF)
                .and().formLogin().successHandler(successHandler)
                .loginPage("/").usernameParameter("email").passwordParameter("password")
                .and().logout().permitAll();
    }
}
