package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<Ec2Client> proxyClient,
    final Logger logger);

  protected DescribeCapacityReservationsResponse describeCapacityReservations(
          final DescribeCapacityReservationsRequest describeRequest,
          final ProxyClient<Ec2Client> proxyClient,
          final Logger logger) {
    DescribeCapacityReservationsResponse reservationsResponse = null;
    try {
      logger.log("[INFO] Reading capacity reservation");
      reservationsResponse = proxyClient.injectCredentialsAndInvokeV2(describeRequest,
              (proxyRequest) -> proxyClient.client().describeCapacityReservations(proxyRequest));
      logger.log(String.format("[INFO] Describe capacity reservation response %s", reservationsResponse));
      //A read handler MUST return FAILED with a NotFound error code if the resource does not exist.
      if (reservationsResponse.capacityReservations().get(0).state().toString().equalsIgnoreCase("cancelled")) {
        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeRequest.capacityReservationIds().get(0));
      }
    } catch (final Exception e) {
      logger.log("[ERROR] Error while describing capacity reservation");
      throw e;
    }
    return reservationsResponse;
  }
}
