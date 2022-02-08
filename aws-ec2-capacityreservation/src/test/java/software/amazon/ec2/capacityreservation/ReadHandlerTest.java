package software.amazon.ec2.capacityreservation;

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
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
    public void test_simple_describe() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-101")
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

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(cr.capacityReservationId()).isEqualTo(response.getResourceModel().getId());
    }

    @Test
    public void test_describing_non_existing_cr() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-101")
                .build();

        final AwsServiceException serviceException = AwsServiceException.builder().message("serviceException").build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenThrow(serviceException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Test
    public void test_reading_cancelled_cr() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder()
                .id("cr-101")
                .build();
        final CapacityReservation cr = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("cancelled")
                .totalInstanceCount(1)
                .build();

        final DescribeCapacityReservationsResponse describeResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(cr)
                .build();

        when(ec2Client.describeCapacityReservations(any(DescribeCapacityReservationsRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Assertions.assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });
    }
}
