# AWS::EC2::CapacityReservation

Resource Type definition for AWS::EC2::CapacityReservation

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::EC2::CapacityReservation",
    "Properties" : {
        "<a href="#tenancy" title="Tenancy">Tenancy</a>" : <i>String</i>,
        "<a href="#enddatetype" title="EndDateType">EndDateType</a>" : <i>String</i>,
        "<a href="#tagspecifications" title="TagSpecifications">TagSpecifications</a>" : <i>[ <a href="tagspecification.md">TagSpecification</a>, ... ]</i>,
        "<a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>" : <i>String</i>,
        "<a href="#enddate" title="EndDate">EndDate</a>" : <i>String</i>,
        "<a href="#ebsoptimized" title="EbsOptimized">EbsOptimized</a>" : <i>Boolean</i>,
        "<a href="#outpostarn" title="OutPostArn">OutPostArn</a>" : <i>String</i>,
        "<a href="#instancecount" title="InstanceCount">InstanceCount</a>" : <i>Integer</i>,
        "<a href="#placementgrouparn" title="PlacementGroupArn">PlacementGroupArn</a>" : <i>String</i>,
        "<a href="#instanceplatform" title="InstancePlatform">InstancePlatform</a>" : <i>String</i>,
        "<a href="#instancetype" title="InstanceType">InstanceType</a>" : <i>String</i>,
        "<a href="#ephemeralstorage" title="EphemeralStorage">EphemeralStorage</a>" : <i>Boolean</i>,
        "<a href="#instancematchcriteria" title="InstanceMatchCriteria">InstanceMatchCriteria</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::EC2::CapacityReservation
Properties:
    <a href="#tenancy" title="Tenancy">Tenancy</a>: <i>String</i>
    <a href="#enddatetype" title="EndDateType">EndDateType</a>: <i>String</i>
    <a href="#tagspecifications" title="TagSpecifications">TagSpecifications</a>: <i>
      - <a href="tagspecification.md">TagSpecification</a></i>
    <a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>: <i>String</i>
    <a href="#enddate" title="EndDate">EndDate</a>: <i>String</i>
    <a href="#ebsoptimized" title="EbsOptimized">EbsOptimized</a>: <i>Boolean</i>
    <a href="#outpostarn" title="OutPostArn">OutPostArn</a>: <i>String</i>
    <a href="#instancecount" title="InstanceCount">InstanceCount</a>: <i>Integer</i>
    <a href="#placementgrouparn" title="PlacementGroupArn">PlacementGroupArn</a>: <i>String</i>
    <a href="#instanceplatform" title="InstancePlatform">InstancePlatform</a>: <i>String</i>
    <a href="#instancetype" title="InstanceType">InstanceType</a>: <i>String</i>
    <a href="#ephemeralstorage" title="EphemeralStorage">EphemeralStorage</a>: <i>Boolean</i>
    <a href="#instancematchcriteria" title="InstanceMatchCriteria">InstanceMatchCriteria</a>: <i>String</i>
</pre>

## Properties

#### Tenancy

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EndDateType

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TagSpecifications

_Required_: No

_Type_: List of <a href="tagspecification.md">TagSpecification</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AvailabilityZone

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EndDate

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EbsOptimized

_Required_: No

_Type_: Boolean

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### OutPostArn

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InstanceCount

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PlacementGroupArn

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InstancePlatform

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InstanceType

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EphemeralStorage

_Required_: No

_Type_: Boolean

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InstanceMatchCriteria

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

Returns the <code>Id</code> value.

#### AvailableInstanceCount

Returns the <code>AvailableInstanceCount</code> value.

#### TotalInstanceCount

Returns the <code>TotalInstanceCount</code> value.
