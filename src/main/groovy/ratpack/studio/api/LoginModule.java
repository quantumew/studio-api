package ratpack.studio.api;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class LoginModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LoginService.class).in(Scopes.SINGLETON);
        bind(UserDbCommands.class).in(Scopes.SINGLETON);
    }
}
