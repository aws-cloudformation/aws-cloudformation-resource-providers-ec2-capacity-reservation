package software.amazon.ec2.capacityreservation;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.ec2.model.CancelCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.awssdk.services.ec2.model.ModifyCapacityReservationRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  public final static String RESOURCE_TYPE_CAPACITY_RESERVATION = "capacity-reservation";
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
            .endDate(getEndDate(model.getEndDate()))
            .endDateType(model.getEndDateType())
            .ephemeralStorage(model.getEphemeralStorage())
            .instanceCount(model.getInstanceCount())
            .instanceMatchCriteria(model.getInstanceMatchCriteria())
            .instancePlatform(model.getInstancePlatform())
            .tenancy(model.getTenancy())
            .instanceType(model.getInstanceType());

    //TODO Get tags from resource model and inject into the request
  /*  final List<TagSpecification> tags = consolidateTags(handlerRequest, model, logger);
    if (!tags.isEmpty()) {
      builder.tagSpecifications(tags);
    }*/

    //TODO Add following attribute after fixing sdk issue (Currently latest SDKs are throwing exceptions)
    //.placementGroup.(model.getPlacementGroupArn())
    //.outpostArn(model.getOutPostArn())
    return builder.build();
  }

  private static Instant getEndDate(String endDate) {
    if (endDate == null) {
      return null;
    }
    return Instant.parse(endDate);
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
    return DescribeCapacityReservationsRequest.builder().capacityReservationIds(crID).build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param reservationsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeCapacityReservationsResponse reservationsResponse) {
    final CapacityReservation cr = reservationsResponse.capacityReservations().get(0);
    final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder();
    builder.id(cr.capacityReservationId())
            .availabilityZone(cr.availabilityZone())
            .availableInstanceCount(cr.availableInstanceCount())
            .ebsOptimized(cr.ebsOptimized())
            .endDate(String.valueOf(cr.endDate()))
            .endDateType(String.valueOf(cr.endDateType()))
            .ephemeralStorage(cr.ephemeralStorage())
            .instanceCount(cr.totalInstanceCount())
            .instanceMatchCriteria(cr.instanceMatchCriteriaAsString())
            .instancePlatform(String.valueOf(cr.instancePlatform()))
            .instanceType(cr.instanceType())
            .tenancy(cr.tenancyAsString())
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
      request.endDate(getEndDate(model.getEndDate()));
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
    return streamOfOrEmpty(awsResponse.capacityReservations())
            .map(resource -> ResourceModel.builder()
                    // include only primary identifier
                    .id(resource.capacityReservationId())
                    .build())
            .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
  }

  /**
   * Request to add tags to a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static AwsRequest tagResourceRequest(final ResourceModel model, final Map<String, String> addedTags) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
    return awsRequest;
  }

  /**
   * Request to add tags to a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static AwsRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
    return awsRequest;
  }

  private static List<TagSpecification> consolidateTags(final ResourceHandlerRequest<ResourceModel> handlerRequest,
                                                        final ResourceModel model,
                                                        final Logger logger) {
    final List<Tag> tags = new ArrayList<>();
    final List<TagSpecification> crTagSpecification = new ArrayList<>();
    // Get stack-level tags from CFN
    if (handlerRequest.getDesiredResourceTags() != null) {
      handlerRequest.getDesiredResourceTags().forEach((key, value) -> {
        Tag tag = Tag.builder()
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
    } else {
      logger.log("CFN stack-level and system tags : " + tags);
      crTagSpecification.add(TagSpecification.builder().resourceType(RESOURCE_TYPE_CAPACITY_RESERVATION).tags(tags).build());
    }

    //get user-provided tags
    if (model.getTagSpecifications() != null && model.getTagSpecifications().size() > 0) {
      crTagSpecification.addAll(model.getTagSpecifications().stream()
              .map(spec -> TagSpecification.builder().resourceType(spec.getResourceType()).tags(spec.getTags().stream()
                      .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                      .collect(Collectors.toList())).build()).collect(Collectors.toList()));
    }
    logger.log("TagSpecifications to add : " + crTagSpecification);
    return crTagSpecification;
  }
}
