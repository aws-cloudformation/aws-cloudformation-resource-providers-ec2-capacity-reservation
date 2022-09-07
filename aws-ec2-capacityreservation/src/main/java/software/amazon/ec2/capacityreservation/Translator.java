package software.amazon.ec2.capacityreservation;

import lombok.SneakyThrows;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.model.CancelCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.awssdk.services.ec2.model.ModifyCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  private static final String CR_RESOURCE_TYPE = "capacity-reservation";

  /**
   * Request to create a resource
   * @param model resource model
   * @param handlerRequest
   * @param logger
   * @return awsRequest the aws service request to create a resource
   */
  static CreateCapacityReservationRequest translateToCreateRequest(final ResourceModel model,
                                                                   final ResourceHandlerRequest<ResourceModel> handlerRequest,
                                                                   final Logger logger) {
      final CreateCapacityReservationRequest.Builder builder = CreateCapacityReservationRequest.builder()
              .availabilityZone(model.getAvailabilityZone())
              .clientToken(handlerRequest.getClientRequestToken())
              .ebsOptimized(model.getEbsOptimized())
              .endDate(getEndDate(model.getEndDate(), logger))
              .endDateType(model.getEndDateType())
              .ephemeralStorage(model.getEphemeralStorage())
              .instanceCount(model.getInstanceCount())
              .instanceMatchCriteria(model.getInstanceMatchCriteria())
              .instancePlatform(model.getInstancePlatform())
              .tenancy(model.getTenancy())
              .instanceType(model.getInstanceType())
              .placementGroupArn(model.getPlacementGroupArn())
              .outpostArn(model.getOutPostArn());

      final List<software.amazon.awssdk.services.ec2.model.TagSpecification> tags = consolidateTags(handlerRequest, model, logger);
      if (tags != null && tags.size() > 0) {
        builder.tagSpecifications(tags);
      }
      return builder.build();
  }

  @SneakyThrows
  private static Instant getEndDate(final String endDate, final Logger logger) {
    if (endDate == null) {
      return null;
    }

    try {
      return Instant.parse(endDate);
    } catch (Exception e) {
      logger.log(String.format("Instance parse failed with %s", e));
    }

    //This is for backward compatibility
    try {
      logger.log(String.format("Parsing the date using SimpleDateFormat [E MMM dd HH:mm:ss z yyyy]"));
      SimpleDateFormat oldDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
      return oldDateFormat.parse(endDate).toInstant();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeCapacityReservationsRequest translateToReadRequest(final ResourceModel model, final Logger logger) {
    final String crID = model.getId();
    logger.log(String.format("Capacity reservation ID = %s", crID));
    if (crID == null) {
      logger.log("Capacity reservation ID is null throwing exception");
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
    }
    return DescribeCapacityReservationsRequest.builder()
            .capacityReservationIds(crID)
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param reservationsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeCapacityReservationsResponse reservationsResponse, final Logger logger) {
    final CapacityReservation cr = reservationsResponse.capacityReservations().get(0);
    final List<Tag> tags = cr.tags().stream().map(tag -> Tag.builder()
            .key(tag.key())
            .value(tag.value())
            .build()).collect(Collectors.toList());
    final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder();
    builder.id(cr.capacityReservationId())
            .availabilityZone(cr.availabilityZone())
            .availableInstanceCount(cr.availableInstanceCount())
            .ebsOptimized(cr.ebsOptimized())
            .endDate(String.valueOf(cr.endDate()))
            .endDateType(cr.endDateTypeAsString())
            .ephemeralStorage(cr.ephemeralStorage())
            .totalInstanceCount(cr.totalInstanceCount())
            .instanceMatchCriteria(cr.instanceMatchCriteriaAsString())
            .instancePlatform(cr.instancePlatformAsString())
            .instanceType(cr.instanceType())
            .tenancy(cr.tenancyAsString())
            .placementGroupArn(cr.placementGroupArn())
            .outPostArn(cr.outpostArn())
            .tagSpecifications(Arrays.asList(TagSpecification.builder()
                    .resourceType(CR_RESOURCE_TYPE).tags(tags).build()))
            .build();
    final ResourceModel model = builder.build();
    return model;
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static CancelCapacityReservationRequest translateToDeleteRequest(final ResourceModel model, final Logger logger) {
    final String crID = model.getId();
    logger.log(String.format("Capacity reservation ID = %s", crID));
    if (crID == null) {
      logger.log("Capacity reservation ID is null throwing exception");
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
    }
    final CancelCapacityReservationRequest request = CancelCapacityReservationRequest.builder()
            .capacityReservationId(crID)
            .build();
    return request;
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyCapacityReservationRequest translateToUpdateRequest(final ResourceModel model, final Logger logger) {
    final String crID = model.getId();
    logger.log(String.format("Capacity reservation ID = %s", crID));
    if (crID == null) {
      logger.log("Capacity reservation ID is null throwing exception");
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
    }
    logger.log("Model : " + model.toString());
    final ModifyCapacityReservationRequest.Builder request = ModifyCapacityReservationRequest.builder()
            .capacityReservationId(crID);
    if (model.getEndDate() != null) {
      request.endDate(getEndDate(model.getEndDate(), logger));
    }
    if (model.getEndDateType() != null) {
      request.endDateType(model.getEndDateType());
    }
    if (model.getInstanceCount() != null) {
      request.instanceCount(model.getInstanceCount());
    }
    return request.build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static DescribeCapacityReservationsRequest translateToListRequest(final String nextToken) {
    return DescribeCapacityReservationsRequest.builder()
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListResponse(final DescribeCapacityReservationsResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
    return streamOfOrEmpty(awsResponse.capacityReservations()).filter(capacityReservation -> !capacityReservation.state().toString().equalsIgnoreCase("cancelled"))
            .map(resource -> ResourceModel.builder()
                    // include only primary identifier
                    .id(resource.capacityReservationId())
                    .build())
            .collect(Collectors.toList());
  }

  static ProgressEvent<ResourceModel, CallbackContext> translateError(final Exception ex) {
    if (ex instanceof CfnNotFoundException) {
      return ProgressEvent.defaultFailureHandler(ex, HandlerErrorCode.NotFound);
    } else if (ex instanceof AwsServiceException) {
      final AwsServiceException serviceException = (AwsServiceException) ex;
      if (serviceException.statusCode() == 500) {
        return ProgressEvent.defaultFailureHandler(ex, HandlerErrorCode.ServiceInternalError);
      } else if (serviceException.statusCode() == 400) {
        return ProgressEvent.defaultFailureHandler(ex, HandlerErrorCode.InvalidRequest);
      }
    }
    return ProgressEvent.defaultFailureHandler(ex, HandlerErrorCode.GeneralServiceException);
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
  }

  private static List<software.amazon.awssdk.services.ec2.model.TagSpecification> consolidateTags(final ResourceHandlerRequest<ResourceModel> handlerRequest,
                                                                                                  final ResourceModel model,
                                                                                                  final Logger logger) {
    final List<software.amazon.awssdk.services.ec2.model.Tag> tags = new ArrayList<>();
    final List<software.amazon.awssdk.services.ec2.model.TagSpecification> crTagSpecs = new ArrayList<>();

    // Get stack-level tags from CFN
    if (handlerRequest.getDesiredResourceTags() != null) {
      handlerRequest.getDesiredResourceTags().forEach((key, value) -> {
        software.amazon.awssdk.services.ec2.model.Tag tag = software.amazon.awssdk.services.ec2.model.Tag.builder()
                .key(key)
                .value(value)
                .build();
        tags.add(tag);
      });
    }

    /* Get CFN system tags https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-resource-tags.html
      aws:cloudformation:logical-id
      aws:cloudformation:stack-id
      aws:cloudformation:stack-name
     */
    if (handlerRequest.getSystemTags() != null) {
      handlerRequest.getSystemTags().forEach((key, value) -> {
        software.amazon.awssdk.services.ec2.model.Tag tag = software.amazon.awssdk.services.ec2.model.Tag.builder()
                .key(key)
                .value(value)
                .build();
        tags.add(tag);
      });
    }

    if (tags.isEmpty()) {
      logger.log("No stack-level tags and system tags for CFN");
    }


    // Get user-provided tags
    if (model.getTagSpecifications() != null && model.getTagSpecifications().size() > 0) {

      for(TagSpecification tagSpecification : model.getTagSpecifications()){
        if(tagSpecification.getResourceType().equalsIgnoreCase(CR_RESOURCE_TYPE)){
          tags.addAll(tagSpecification.getTags().stream().map(tag -> software.amazon.awssdk.services.ec2.model.Tag.builder()
                  .key(tag.getKey())
                  .value(tag.getValue())
                  .build()).collect(Collectors.toList()));
        } else{
          crTagSpecs.add(software.amazon.awssdk.services.ec2.model.TagSpecification.builder()
                  .resourceType(tagSpecification.getResourceType())
                  .tags(tagSpecification.getTags().stream().map(tag -> software.amazon.awssdk.services.ec2.model.Tag.builder()
                          .key(tag.getKey())
                          .value(tag.getValue())
                          .build()).collect(Collectors.toList()))
                  .build());
        }
      }
    }
    if(tags.isEmpty()){
      return crTagSpecs;
    }

    //Add resource tags
    crTagSpecs.add(software.amazon.awssdk.services.ec2.model.TagSpecification.builder()
            .resourceType(ResourceType.CAPACITY_RESERVATION)
            .tags(tags)
            .build());

    logger.log("TagSpecifications to add : " + crTagSpecs);
    return crTagSpecs;
  }
}
