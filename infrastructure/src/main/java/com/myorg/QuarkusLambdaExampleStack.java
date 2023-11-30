package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.Map;

public class QuarkusLambdaExampleStack extends Stack {

    public QuarkusLambdaExampleStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        //TODO: Add snapstart, does architecture than hava to be X86?
        //Memory size 1700 equals 1 cpu
        var function = Function.Builder.create(this, Constants.FUNCTION_NAME)
                .functionName(Constants.FUNCTION_NAME)
                .runtime(Runtime.JAVA_21)
                .architecture(Architecture.ARM_64)
                .memorySize(1700)
                .code(Code.fromAsset(Defaults.functionZip))
                .handler(Defaults.quarkusFunctionHandler)
                .timeout(Duration.seconds(Defaults.lambdaTimeout))
                .environment(Map.of(
                        "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"))
                .build();
        
    }
}
