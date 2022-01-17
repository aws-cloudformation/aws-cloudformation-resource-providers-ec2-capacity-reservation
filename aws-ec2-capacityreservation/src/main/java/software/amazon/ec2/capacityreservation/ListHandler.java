package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<Ec2Client> client,
            final Logger logger) {
        List<ResourceModel> models = new ArrayList<>();

        // STEP 1 [construct a body of a request]
        final DescribeCapacityReservationsRequest reservationsRequest = Translator.translateToListRequest(request.getNextToken());

        // STEP 2 [make an api call]
        DescribeCapacityReservationsResponse awsResponse = null;
        // STEP 3 [get a token for the next page]
        String nextToken = null;
        try {
            awsResponse = proxy.injectCredentialsAndInvokeV2(reservationsRequest, (proxyRequest) -> client.client().describeCapacityReservations(proxyRequest));
            nextToken = awsResponse.nextToken();
            models = Translator.translateFromListRequest(awsResponse);
        } catch (Exception e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
        // STEP 4 [ construct resource models]
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/master/aws-logs-loggroup/src/main/java/software/amazon/logs/loggroup/ListHandler.java#L19-L21
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
