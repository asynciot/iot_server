import com.google.inject.AbstractModule;

import services.accounts.AccountInitializer;

public class AccountModule  extends AbstractModule {
    @Override
    public void configure() {
        bind(AccountInitializer.class).asEagerSingleton();
//        bind(ApplicationTimer.class).asEagerSingleton();
//        bind(AnalysisManager.class).asEagerSingleton();
    }

}
