package com.test.bank;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.configuration.bundle.TypesafeConfigurationBundle;
import com.test.bank.dagger.BootstrapComponent;
import com.test.bank.dagger.BootstrapModule;
import com.test.bank.dagger.DaggerBootstrapComponent;
import com.test.bank.initializer.DataSourceInitializer;
import com.test.bank.initializer.JerseyInitializer;
import com.test.bank.tool.config.EnvConfigManager;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.inject.Inject;

public class MicroServiceApplication extends Application<MicroServiceApplication.DropwizardConfiguration> {

    @Inject
    DataSourceInitializer dataSourceInitializer;

    @Inject
    EnvConfigManager envConfigManager;

    public BootstrapComponent bootstrapComponent;

    public BootstrapModule bootstrapModule;

    static class DropwizardConfiguration extends TypesafeConfiguration {
    }

    public static void main(String[] args) throws Exception {
        new MicroServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<DropwizardConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(new TypesafeConfigurationBundle("dropwizard"));
    }

    @Override
    public void run(DropwizardConfiguration configuration, Environment environment) {
        bootstrapModule = new BootstrapModule();
        bootstrapComponent = DaggerBootstrapComponent.builder()
                .bootstrapModule(bootstrapModule)
                .build();
        bootstrapComponent.inject(this);

        dataSourceInitializer.initialize();
        new JerseyInitializer(bootstrapComponent).initialize(environment);
    }


}
