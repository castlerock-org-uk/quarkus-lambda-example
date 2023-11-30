package com.myorg;

public final class Config {

    private Config() {
        //Private constructor to prevent instantiation
    }

    public static final String functionZip = "../software/lambda-example/target/function.zip";
    public static final String quarkusFunctionHandler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
    public static final int lambdaTimeout = 10;

    public static final String FUNCTION_NAME = "testFunction";

}
