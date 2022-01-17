package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CancelCapacityReservationResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/cancel-capacity-reservation.html
 */
public class DeleteHandler extends BaseHandlerStd {
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
                // if target API does not support 'ResourceNotFoundException' then following check is required
                .then(progress ->
                        proxy.initiate("AWS-EC2-CapacityReservation::Delete::PreDeletionCheck", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(model -> Translator.translateToReadRequest(model, logger))
                                .makeServiceCall((describeCapacityReservationsRequest, ec2client) -> describeCapacityReservations(describeCapacityReservationsRequest, ec2client, logger))
                                .handleError((describeCapacityReservationsRequest, exception, ec2client, model, context) -> ProgressEvent.failed(model, context, HandlerErrorCode.InvalidRequest, exception.getMessage()))
                                .progress()
                )

                // STEP 2.0 [delete/stabilize progress chain - required for resource deletion]
                .then(progress ->
                        proxy.initiate("AWS-EC2-CapacityReservation::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                                // STEP 2.1 [construct a body of a request]
                                .translateToServiceRequest((model) -> Translator.translateToDeleteRequest(model, logger))
                                // STEP 2.2 [making cancel api call]
                                .makeServiceCall((cancelCapacityReservationRequest, ec2client) -> {
                                    CancelCapacityReservationResponse cancelResponse = null;
                                    try {
                                        cancelResponse = ec2client.injectCredentialsAndInvokeV2(cancelCapacityReservationRequest, ec2client.client()::cancelCapacityReservation);
                                        logger.log(String.format("[INFO] Successfully cancelled capacity reservation: %s", cancelCapacityReservationRequest.capacityReservationId()));
                                    } catch (final AwsServiceException e) {
                                        logger.log(String.format("[ERROR] Error occurred during cancellation : %s", e.getMessage()));
                                        throw e;
                                    }
                                    logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                                    logger.log(String.format(" Returned value %s", cancelResponse.returnValue()));
                                    return cancelResponse;
                                })
                                // STEP 2.3 [Stabilize the resource, not really required for ODCR]
                                .stabilize((cancelCapacityReservationRequest, cancelResponse, ec2client, model, context) -> {
                                    // No stabilization code is required
                                    final boolean stabilized = true;
                                    logger.log(String.format("%s [%s] deletion has stabilized: %s", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), stabilized));
                                    logger.log(String.format(" Returned value %s", cancelResponse.returnValue()));
                                    return stabilized;
                                })
                                // STEP 2.4 [Handle error]
                                .handleError((cancelCapacityReservationRequest, exception, ec2client, model, context) -> ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException))
                                .progress()
                )
                // STEP 3 [return the successful progress event without resource model]
                .then(progress -> ProgressEvent.success(progress.getResourceModel(), callbackContext));
    }
}
