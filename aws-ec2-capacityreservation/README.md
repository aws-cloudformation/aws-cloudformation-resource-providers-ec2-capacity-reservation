# AWS::EC2::CapacityReservation

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.


## Testing locally
1. Start docker and enable file sharing for the workspace (No need to launch the container or app)
2. Start sam local lambda in the resource directory in a new terminal `sam local start-lambda`
3. Call the handler eg `cfn invoke CREATE local-tests/create-payload.json`

Note : The log output will be shown in the sam lambda terminal

## Testing in your aws account
1. Build package `mvn package`
2. Upload the build to your aws account `cfn submit --set-default --no-role`
3. Create CFN stack with capacity reservation resource
4. Let the stack run it’s cycle
5. You could see the logs in the cloud watch log group

NOTE : You could also specify the region while submitting  `cfn submit --region us-east-1 --set-default --no-role`


### List Resource Registrations

```
aws cloudformation list-type-versions --type RESOURCE --type-name AWS::EC2::CapacityReservation --region us-east-1
```

If you've run the `cfn submit` command multiple times, there should be multiple results from this api, with one of the results the `default`

### Deregister Resource

If you're done testing, and would like your cloudformation commands to be handled by the official, published version of the resource handler, you will have to deregister any development resource handler versions:

First deregister all non-default versions (version id should be in the `list-type-versions` output):
```
aws cloudformation deregister-type --type RESOURCE --type-name AWS::EC2::CapacityReservation --region us-east-1 --version-id 00000006
```

Then, when only the default version is left:

```
aws cloudformation deregister-type --type RESOURCE --type-name AWS::EC2::CapacityReservation --region us-east-1
```

### Running contract tests

``NOTE : update AvailabilityZone in inputs/<file>.json from {{ExportedAvailabilityZone}} to the desired one``

Start local lambda
```
sam local start-lambda
```
Run tests
```
cfn test --enforce-timeout 240
```
Run individual tests
```
cfn test --enforce-timeout 240 -- -k contract_update_without_create
```
