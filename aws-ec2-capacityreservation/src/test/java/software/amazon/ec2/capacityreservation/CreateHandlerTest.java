package software.amazon.ec2.capacityreservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

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

    @Test
    public void simple_create_handle_request() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .instanceType("t2.micro")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .instanceCount(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("active")
                .totalInstanceCount(1)
                .build();
        final CreateCapacityReservationResponse createResponse = CreateCapacityReservationResponse.builder()
                .capacityReservation(cr).build();

        when(ec2Client.createCapacityReservation(any(CreateCapacityReservationRequest.class))).thenReturn(createResponse);
        final DescribeCapacityReservationsResponse reservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(reservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(cr.capacityReservationId()).isEqualTo(response.getResourceModel().getId());
        assertThat(cr.availabilityZone()).isEqualTo(response.getResourceModel().getAvailabilityZone());
        assertThat(cr.instanceType()).isEqualTo(response.getResourceModel().getInstanceType());
    }

    @Test
    public void create_CR_with_end_date() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .instanceType("t2.micro")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .instanceCount(1)
                .endDateType("limited")
                .endDate("2124-08-31T23:59:59Z")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("active")
                .totalInstanceCount(1)
                .build();
        final CreateCapacityReservationResponse createResponse = CreateCapacityReservationResponse.builder()
                .capacityReservation(cr).build();

        when(ec2Client.createCapacityReservation(any(CreateCapacityReservationRequest.class))).thenReturn(createResponse);
        final DescribeCapacityReservationsResponse reservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(reservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(cr.capacityReservationId()).isEqualTo(response.getResourceModel().getId());
        assertThat(cr.availabilityZone()).isEqualTo(response.getResourceModel().getAvailabilityZone());
        assertThat(cr.instanceType()).isEqualTo(response.getResourceModel().getInstanceType());
        assertThat(cr.endDateType()).isEqualTo(response.getResourceModel().getEndDateType());
    }

    @Test
    public void create_CR_with_end_date_backwards_compatible() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .instanceType("t2.micro")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .instanceCount(1)
                .endDateType("limited")
                .endDate("Fri Jan 01 00:00:00 GMT 2100")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("active")
                .totalInstanceCount(1)
                .build();
        final CreateCapacityReservationResponse createResponse = CreateCapacityReservationResponse.builder()
                .capacityReservation(cr).build();

        when(ec2Client.createCapacityReservation(any(CreateCapacityReservationRequest.class))).thenReturn(createResponse);
        final DescribeCapacityReservationsResponse reservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(reservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(cr.capacityReservationId()).isEqualTo(response.getResourceModel().getId());
        assertThat(cr.availabilityZone()).isEqualTo(response.getResourceModel().getAvailabilityZone());
        assertThat(cr.instanceType()).isEqualTo(response.getResourceModel().getInstanceType());
        assertThat(cr.endDateType()).isEqualTo(response.getResourceModel().getEndDateType());
    }

    @Test
    public void create_handle_request_with_Tags() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .instanceType("t2.micro")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .instanceCount(1)
                .tagSpecifications(Arrays.asList(TagSpecification.builder()
                                .tags(Arrays.asList(Tag.builder()
                                        .key("TestKey")
                                        .value("TestValue").build()
                                ))
                                .resourceType("capacity-reservation").build(),
                        TagSpecification.builder()
                                .tags(Arrays.asList(Tag.builder()
                                        .key("TestKey2")
                                        .value("TestValue2").build()
                                ))
                                .resourceType("ec2-instance").build()))
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("active")
                .totalInstanceCount(1)
                .build();
        final CreateCapacityReservationResponse createResponse = CreateCapacityReservationResponse.builder()
                .capacityReservation(cr).build();

        when(ec2Client.createCapacityReservation(any(CreateCapacityReservationRequest.class))).thenReturn(createResponse);
        final DescribeCapacityReservationsResponse reservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(reservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(cr.capacityReservationId()).isEqualTo(response.getResourceModel().getId());
        assertThat(cr.availabilityZone()).isEqualTo(response.getResourceModel().getAvailabilityZone());
        assertThat(cr.instanceType()).isEqualTo(response.getResourceModel().getInstanceType());
    }

    @Test
    public void handle_when_ODCR_Throws_Exception() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .instanceType("t2.micro")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .instanceCount(1)
                .tagSpecifications(Arrays.asList(TagSpecification.builder()
                        .tags(Arrays.asList(Tag.builder()
                                .key("TestKey")
                                .value("TestValue").build()
                        ))
                        .resourceType("capacity-reservation").build()))
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final AwsServiceException serviceException = AwsServiceException.builder().message("serviceException").build();

        when(ec2Client.createCapacityReservation(any(CreateCapacityReservationRequest.class))).thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    public void handle_incorrect_client_parameter() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .instancePlatform("Windows")
                .instanceCount(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
