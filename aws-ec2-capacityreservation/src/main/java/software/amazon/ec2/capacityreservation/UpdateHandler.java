package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ModifyCapacityReservationResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/modify-capacity-reservation.html
 */
public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<Ec2Client> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)

                // STEP 1 [check if resource already exists]
                .then(progress ->
                        proxy.initiate("AWS-EC2-CapacityReservation::Update::PreUpdateCheck", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                                .translateToServiceRequest((model) -> Translator.translateToReadRequest(model, logger))
                                .makeServiceCall((describeCapacityReservationsRequest, ec2client) -> describeCapacityReservations(describeCapacityReservationsRequest, ec2client, logger))
                                .handleError((awsRequest, exception, client, model, context) -> Translator.translateError(exception))
                                .progress())
                // STEP 2 [first update/stabilize progress chain - required for resource update]
                .then(progress ->
                        // STEP 2.0 [initialize a proxy context]
                        proxy.initiate("AWS-EC2-CapacityReservation::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                // STEP 2.1 [Construct body of modify request]
                                .translateToServiceRequest((model) -> Translator.translateToUpdateRequest(model, logger))
                                // STEP 2.2 [make an api call]
                                .makeServiceCall((awsRequest, client) -> {
                                    ModifyCapacityReservationResponse reservationResponse = null;
                                    try {
                                        reservationResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::modifyCapacityReservation);
                                    } catch (final AwsServiceException e) {
                                        logger.log(String.format("%s has thrown error in Update.", ResourceModel.TYPE_NAME));
                                        throw e;
                                    }
                                    logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                                    return reservationResponse;
                                })
                                // STEP 2.3 [stabilize step is not necessarily required but typically involves describing the resource until it is in a certain status, though it can take many forms]
                                .stabilize((awsRequest, awsResponse, client, model, context) -> {
                                    //No stabilization code is required for update
                                    final boolean stabilized = true;
                                    logger.log(String.format("%s [%s] update has stabilized: %s", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), stabilized));
                                    return stabilized;
                                })
                                // STEP 2.4 [Handle errors]
                                .handleError((modifyCapacityReservationRequest, exception, ec2client, model, context) -> Translator.translateError(exception))
                                .progress())
                // STEP 3 [describe call/chain to return the resource model]
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
