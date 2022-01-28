package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/create-capacity-reservation.html
 */
public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Ec2Client> proxyClient,
        final Logger logger) {

    this.logger = logger;
    return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                    // STEP 1 [initialize a proxy context]
                    proxy.initiate("AWS-EC2-CapacityReservation::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                            // STEP 2 [construct a body of create capacity reservation request]
                            .translateToServiceRequest((model) -> Translator.translateToCreateRequest(model, request, logger))
                            // STEP 3 [Make create capacity reservation api call]
                            .makeServiceCall((createCapacityReservationRequest, ec2client) -> {
                                logger.log(String.format("[INFO] Creating resource with request: %s", request.toString()));
                                CreateCapacityReservationResponse createCapacityReservationResponse = null;
                                try {
                                    createCapacityReservationResponse = ec2client.injectCredentialsAndInvokeV2(createCapacityReservationRequest,
                                            ec2client.client()::createCapacityReservation);
                                    logger.log(String.format("[INFO] createCapacityReservation response : ", createCapacityReservationResponse));
                                } catch (final Exception e) {
                                    throw e;
                                }
                                logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
                                return createCapacityReservationResponse;
                            })
                            // STEP 4 [stabilize step is not necessarily required but typically involves describing the resource until it is in a certain status]
                            // We also set cr ID to resource model from aws response
                            .stabilize((createCapacityReservationRequest, createCapacityReservationResponse, client, model, context) -> {
                                final boolean stabilized = false;
                                model.setId(createCapacityReservationResponse.capacityReservation().capacityReservationId());
                                model.setInstanceMatchCriteria(createCapacityReservationResponse.capacityReservation().instanceMatchCriteriaAsString());
                                model.setTenancy(createCapacityReservationResponse.capacityReservation().tenancyAsString());
                                logger.log(String.format("CR is in %s state ", createCapacityReservationResponse.capacityReservation().stateAsString()));
                                //ODCR needs to be in active state
                                if(createCapacityReservationResponse.capacityReservation().state().toString().equalsIgnoreCase("active")){
                                    return true;
                                }
                                return stabilized;
                            })
                            .handleError((createCapacityReservationRequest, exception, ec2client, model, context) -> Translator.translateError(exception))
                            .progress())
            // STEP 5 [describe call/chain to return the resource model]
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
