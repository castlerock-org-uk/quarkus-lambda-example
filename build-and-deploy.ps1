
Write-Output "Building Functions"
Set-Location lambdas\test-lambda
mvn clean package

Write-Output "Building CDK"
Set-Location ..\..\infrastructure
mvn clean package
Write-Output "Deploying"
cdk deploy --profile $args[0]

Set-Location ..\