package software.amazon.ec2.capacityreservation;

import junit.framework.Assert;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void test_simple_list() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final CapacityReservation cr1 = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(1)
                .build();

        final CapacityReservation cr2 = CapacityReservation.builder()
                .capacityReservationId("cr-122")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(3)
                .build();

        DescribeCapacityReservationsResponse describeCapacityReservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(Arrays.asList(cr1,cr2))
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(describeCapacityReservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        Assert.assertEquals(response.getResourceModels().size(), 2);
    }

    @Test
    public void test_list_cancelled_cr() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final CapacityReservation cr1 = CapacityReservation.builder()
                .capacityReservationId("cr-121")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("Active")
                .totalInstanceCount(1)
                .build();

        final CapacityReservation cr2 = CapacityReservation.builder()
                .capacityReservationId("cr-122")
                .availabilityZone("us-east-1a")
                .instancePlatform("Windows")
                .state("cancelled")
                .totalInstanceCount(3)
                .build();

        final DescribeCapacityReservationsResponse describeCapacityReservationsResponse = DescribeCapacityReservationsResponse.builder()
                .capacityReservations(Arrays.asList(cr1,cr2))
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(describeCapacityReservationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        Assert.assertEquals(response.getResourceModels().size(), 1);
    }

    @Test
    public void test_list_ODCR_exception() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final AwsServiceException serviceException = AwsServiceException.builder().message("serviceException").build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenThrow(serviceException);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }
}
