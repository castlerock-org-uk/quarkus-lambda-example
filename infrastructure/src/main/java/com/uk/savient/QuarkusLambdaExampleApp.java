package com.uk.savient;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class QuarkusLambdaExampleApp {

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }

    public static void main(final String[] args) {
        App app = new App();

        Environment environment = makeEnv("409305407868", "eu-west-2");

        new QuarkusLambdaExampleStack(app, "QuarkusLambdaExampleStack", StackProps.builder()
                .env(environment)
                .build());

        app.synth();
    }
}

