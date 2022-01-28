package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-capacity-reservations.html
 */
public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<Ec2Client> proxyClient,
            final Logger logger) {

        this.logger = logger;

        // STEP 1 [initialize a proxy context]
        return proxy.initiate("AWS-EC2-CapacityReservation::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                // STEP 2 [construct body of the request]
                .translateToServiceRequest(model -> Translator.translateToReadRequest(model, logger))
                // STEP 3 [make an api call]
                .makeServiceCall((describeCapacityReservationsRequest, ec2client) -> describeCapacityReservations(describeCapacityReservationsRequest, ec2client, logger))
                // STEP 4 [Handle error]
                .handleError((describeCapacityReservationsRequest, exception, ec2client, model, context) -> Translator.translateError(exception))
                // STEP 5 [gather all properties of the resource]
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse, logger)));
    }
}
