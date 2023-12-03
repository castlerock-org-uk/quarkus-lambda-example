package com.uk.savient;

public final class Config {

    private Config() {
        //Private constructor to prevent instantiation
    }

    public static final String FUNCTION_ZIP = "../lambdas/test-lambda/target/function.zip";
    public static final String QUARKUS_FUNCTION_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
    public static final int LAMBDA_TIMEOUT = 10;

    public static final String FUNCTION_NAME = "testFunction";

}
