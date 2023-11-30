package com.myorg;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.alpha.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpRouteIntegration;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpRouteIntegrationBindOptions;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpRouteIntegrationConfig;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;


import java.util.Map;

import static java.util.Collections.singletonList;

public class QuarkusLambdaExampleStack extends Stack {

    public QuarkusLambdaExampleStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        //TODO: Add snapstart, does architecture than have to be X86?
        //Memory size 1700 equals 1 cpu
        var function = Function.Builder.create(this, Config.FUNCTION_NAME)
                .functionName(Config.FUNCTION_NAME)
                .runtime(Runtime.JAVA_21)
                .architecture(Architecture.ARM_64)
                .memorySize(1700)
                .code(Code.fromAsset(Config.functionZip))
                .handler(Config.quarkusFunctionHandler)
                .timeout(Duration.seconds(Config.lambdaTimeout))
                .environment(Map.of(
                        "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"))
                .build();

        var httpAPI = new HttpApi(this, Config.FUNCTION_NAME + "Api", HttpApiProps.builder()
                .apiName(Config.FUNCTION_NAME + "Api")
                .build());

        var httpLambdaIntegration = HttpLambdaIntegration.Builder.create(Config.FUNCTION_NAME + "Integration", function).build();

        httpAPI.addRoutes(AddRoutesOptions.builder()
                .path("/test")
                .methods(singletonList(HttpMethod.GET))
                .integration(httpLambdaIntegration)
                .build());

        new CfnOutput(this, "api-endpoint", CfnOutputProps.builder()
                .value(httpAPI.getApiEndpoint())
                .build());
    }
}
