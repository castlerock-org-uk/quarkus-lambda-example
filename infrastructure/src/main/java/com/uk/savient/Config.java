package com.uk.savient;

public final class Config {

    private Config() {
        //Private constructor to prevent instantiation
    }

    public static final String EXISTING_VPC_ID = "vpc-00e882f4cbca04364";
    public static final String DB_CREDENTIALS_SECRET = "rainbow/testdbcreds";
    public static final String DB_SECURITY_GROUP_NAME = "test-db-security-group";
    public static final String FUNCTION_ZIP = "../lambdas/test-lambda/target/function.zip";
    public static final String QUARKUS_FUNCTION_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
    public static final int LAMBDA_TIMEOUT = 10;

    public static final String FUNCTION_NAME = "testFunction";

}
