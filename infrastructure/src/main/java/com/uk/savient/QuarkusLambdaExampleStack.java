package com.uk.savient;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.ClusterInstance;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class QuarkusLambdaExampleStack extends Stack {

    public QuarkusLambdaExampleStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var vpc = Vpc.Builder.create(this, "AuroraVpc").build();

        var dbCredentialsSecret = DatabaseSecret.Builder.create(this, "DbCredentialsSecret").username(Config.DB_ADMIN_USER).build();

        var lambdaSecurityGroup = SecurityGroup.Builder.create(this, "LambdaSecurityGroup").vpc(vpc).description("Used by quarkus test lambda").build();

        var dbSecurityGroup = SecurityGroup.Builder.create(this, "DbSecurityGroup").vpc(vpc).description("Allow connection from RDS proxy to DB").build();
        dbSecurityGroup.addIngressRule(lambdaSecurityGroup, Port.tcp(5432), "Posgresql port connection from lambda");

        var postgreSQLAuroraV2 = DatabaseCluster.Builder.create(this, "DbCluster")
                .engine(
                        DatabaseClusterEngine.auroraPostgres(
                                AuroraPostgresClusterEngineProps
                                        .builder()
                                        .version(AuroraPostgresEngineVersion.VER_14_8)
                                        .build()))
                .writer(ClusterInstance.serverlessV2("writer"))
                .serverlessV2MinCapacity(6.5)
                .serverlessV2MaxCapacity(64)
                .securityGroups(List.of(dbSecurityGroup)).defaultDatabaseName(Config.DATABASE_NAME)
                .vpc(vpc)
                .port(5432)
                .credentials(Credentials.fromSecret(dbCredentialsSecret))
                .build();


        //Memory size 1700 equals 1 cpu
        // TODO: Access secret value within lambda itself so it doesn't get exposed below...
        var function = Function.Builder.create(this, Config.FUNCTION_NAME)
                .functionName(Config.FUNCTION_NAME)
                .runtime(Runtime.JAVA_21)
                .architecture(Architecture.X86_64)
                .memorySize(1700)
                .code(Code.fromAsset(Config.FUNCTION_ZIP))
                .handler(Config.QUARKUS_FUNCTION_HANDLER)
                .timeout(Duration.seconds(Config.LAMBDA_TIMEOUT))
                .vpc(vpc)
                .environment(Map.of(
                        "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1",
                        "QUARKUS_DATASOURCE_DB_KIND", "postgresql",
                        "QUARKUS_DATASOURCE_USERNAME", Config.DB_ADMIN_USER,
                        "QUARKUS_DATASOURCE_PASSWORD", dbCredentialsSecret.secretValueFromJson("password").unsafeUnwrap(),
                        "QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://" + postgreSQLAuroraV2.getClusterEndpoint().getSocketAddress() + "/" + Config.DATABASE_NAME))
                .build();

        /*//Enable SnapStart
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
                .build();*/

        postgreSQLAuroraV2.getSecret().grantRead(function);

        var httpLambdaIntegration = HttpLambdaIntegration.Builder.create(Config.FUNCTION_NAME + "Integration", function).build();

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
