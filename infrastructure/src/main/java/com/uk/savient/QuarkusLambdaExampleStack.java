package com.uk.savient;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.CfnFunction;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Version;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QuarkusLambdaExampleStack extends Stack {

    public QuarkusLambdaExampleStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var vpc = Vpc.fromLookup(this, "ExistingVpc", VpcLookupOptions.builder().vpcId(Config.EXISTING_VPC_ID).build());

        var dbCredentialsSecret = Secret.fromSecretNameV2(this, "DbCredentialsSecret", Config.DB_CREDENTIALS_SECRET);

        var dbSecurityGroup = SecurityGroup.Builder.create(this, "DbSecurityGroup")
                .securityGroupName(Config.DB_SECURITY_GROUP_NAME)
                .description("Security group for the Aurora cluster")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        var dbCluster = DatabaseCluster.Builder.create(this, "DbCluster")
                .defaultDatabaseName("quarkus-test")
                .engine(DatabaseClusterEngine.auroraPostgres(AuroraPostgresClusterEngineProps.builder()
                        .version(AuroraPostgresEngineVersion.VER_14_8)
                        .build()))
                .credentials(Credentials.fromSecret(dbCredentialsSecret))
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .securityGroups(List.of(dbSecurityGroup))
                .vpc(vpc)
                .build();

        //Memory size 1700 equals 1 cpu
        var function = Function.Builder.create(this, Config.FUNCTION_NAME)
                .functionName(Config.FUNCTION_NAME)
                .runtime(Runtime.JAVA_21)
                .architecture(Architecture.X86_64)
                .memorySize(1700)
                .code(Code.fromAsset(Config.FUNCTION_ZIP))
                .handler(Config.QUARKUS_FUNCTION_HANDLER)
                .timeout(Duration.seconds(Config.LAMBDA_TIMEOUT))
                .environment(Map.of(
                        "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1",
                        "QUARKUS_DATASOURCE_USERNAME",dbCredentialsSecret.secretValueFromJson("username").toString(),
                        "QUARKUS_DATASOURCE_PASSWORD", dbCredentialsSecret.secretValueFromJson("password").toString(),
                        "QUARKUS_DATASOURCE_JDBC_URL", dbCluster.getClusterEndpoint().toString()))
                .build();

        //Enable SnapStart
        var defaultChild = function.getNode().getDefaultChild();
        if (defaultChild instanceof CfnFunction cfnFunction) {
            cfnFunction.addPropertyOverride("SnapStart", Map.of("ApplyOn", "PublishedVersions"));
        }

        //Enable versioning with alias for latest, needed by SnapStart
        var uniqueLogicalId = "SnapStartVersion_" + LocalDateTime.now();
        var version = Version.Builder.create(this, uniqueLogicalId)
                .lambda(function)
                .description("SnapStart")
                .build();
        var latestFunctionVersion = Alias.Builder.create(this, "SnapstartAlias")
                .aliasName("snapstart")
                .description("this alias is required for SnapStart")
                .version(version)
                .build();

        var httpLambdaIntegration = HttpLambdaIntegration.Builder.create(Config.FUNCTION_NAME + "Integration", latestFunctionVersion).build();

        var httpAPI = new HttpApi(this, Config.FUNCTION_NAME + "Api", HttpApiProps.builder()
                .apiName(Config.FUNCTION_NAME + "Api")
                .defaultIntegration(httpLambdaIntegration)
                .createDefaultStage(true)
                .build());

        new CfnOutput(this, "api-endpoint", CfnOutputProps.builder()
                .value(httpAPI.getApiEndpoint())
                .build());
    }
}
