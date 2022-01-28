package software.amazon.ec2.capacityreservation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.awssdk.services.ec2.model.ModifyCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.ModifyCapacityReservationResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    Ec2Client ec2Client;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        ec2Client = mock(Ec2Client.class);
        proxyClient = MOCK_PROXY(proxy, ec2Client);
    }

    @AfterEach
    public void tear_down() {
    }

    @Test
    public void test_update_end_date() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-121")
                .endDateType("limited")
                .endDate("2022-08-31T23:59:59Z")
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(1)
                .build();

        final DescribeCapacityReservationsResponse describeResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();


        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);

        final ModifyCapacityReservationResponse modifyResponse = ModifyCapacityReservationResponse.builder()
                .returnValue(true)
                .build();
        when(ec2Client.modifyCapacityReservation(any(ModifyCapacityReservationRequest.class))).thenReturn(modifyResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    @Test
    public void test_update_instance_count() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-121")
                .instanceCount(3)
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(3)
                .build();

        final DescribeCapacityReservationsResponse describeResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();


        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);

        final ModifyCapacityReservationResponse modifyResponse = ModifyCapacityReservationResponse.builder()
                .returnValue(true)
                .build();
        when(ec2Client.modifyCapacityReservation(any(ModifyCapacityReservationRequest.class))).thenReturn(modifyResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    @Test
    public void test_updating_cr_without_crId() {
        final UpdateHandler handler = new UpdateHandler();
        final ResourceModel model = ResourceModel.builder()
                .instanceCount(3)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });
    }

    @Test
    public void handle_ODCR_exception() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-121")
                .endDateType("limited")
                .endDate("2022-08-31T23:59:59Z")
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(1)
                .build();

        final DescribeCapacityReservationsResponse describeResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();


        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);


        final AwsServiceException serviceException = AwsServiceException.builder().message("serviceException").build();
        when(ec2Client.modifyCapacityReservation(any(ModifyCapacityReservationRequest.class))).thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Test
    public void handle_ODCR_returns_null_capacity_reservation_id() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-121")
                .endDateType("limited")
                .endDate("2022-08-31T23:59:59Z")
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId(null)
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(1)
                .build();

        final DescribeCapacityReservationsResponse describeResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();


        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);


        final AwsServiceException serviceException = AwsServiceException.builder().message("serviceException").build();
        when(ec2Client.modifyCapacityReservation(any(ModifyCapacityReservationRequest.class))).thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }
}
