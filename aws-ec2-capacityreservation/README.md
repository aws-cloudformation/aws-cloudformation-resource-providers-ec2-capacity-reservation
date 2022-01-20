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
2. Upload the build to your aws account `cfn submit —set-default`
3. Create CFN stack with capacity reservation resource
4. Let the stack run it’s cycle
5. You could see the logs in the cloud watch log group
