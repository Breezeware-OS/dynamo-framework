package net.breezeware.dynamo.iaac.service.config;

import net.breezeware.dynamo.iaac.entity.ServiceProps;

import lombok.Data;
import lombok.EqualsAndHashCode;

import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.BaseService;
import software.amazon.awscdk.services.ecs.ContainerDefinition;
import software.amazon.awscdk.services.ecs.DeploymentCircuitBreaker;
import software.amazon.awscdk.services.ecs.Ec2Service;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.LoadBalancerTargetOptions;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.TaskDefinition;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerRule;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

@Data
@EqualsAndHashCode(callSuper = false)
public class BreezewareServiceConstruct extends Construct {

    private TaskDefinition taskDefinition;
    private LogDriver applicationLogDriver;
    private ILogGroup applicationLogGroup;
    private LogDriver otelLogDriver;
    private ILogGroup otelLogGroup;
    private ContainerDefinition applicationContainerDefinition;
    private ContainerDefinition otelContainerDefinition;
    private BaseService service;
    private ApplicationListenerRule applicationListenerRule;
    private ScalableTaskCount scalableTaskCount;

    public BreezewareServiceConstruct(Construct scope, ServiceProps serviceProps) {
        super(scope, serviceProps.getTaskDefinitionName());
        taskDefinition(serviceProps);
        applicationLogDriver(serviceProps);
        otelLogDriver(serviceProps);
        appicationContainerDefinition(serviceProps);
        otelContainerDefinition(serviceProps);
        service(serviceProps);
        createListenerRule(serviceProps);
        addTargetToTargetGroup(serviceProps);
        createScalableTaskCount(serviceProps);
    }

    private TaskDefinition taskDefinition(ServiceProps serviceProps) {
        taskDefinition = TaskDefinition.Builder.create(this, serviceProps.getTaskDefinitionName() + "TaskDefinition")
                .compatibility(serviceProps.getCompatibility()).memoryMiB(serviceProps.getMemoryMiB())
                .networkMode(serviceProps.getNetworkMode()).cpu(serviceProps.getCpu())
                .executionRole(serviceProps.getExecutionRole()).taskRole(serviceProps.getTaskRole())
                .family(serviceProps.getTaskDefinitionName()).volumes(serviceProps.getVolumes()).build();
        taskDefinition.applyRemovalPolicy(serviceProps.getTaskDefinitionRemovalPolicy());
        return taskDefinition;
    }

    private LogDriver applicationLogDriver(ServiceProps serviceProps) {
        applicationLogGroup = LogGroup.Builder.create(this, serviceProps.getApplicationLogGroupName() + "LogGroup")
                .retention(serviceProps.getApplicationRetentionDays())
                .logGroupName(serviceProps.getApplicationLogGroupName()).build();
        applicationLogGroup.applyRemovalPolicy(serviceProps.getApplicationLogGroupRemovalPolicy());

        applicationLogDriver = LogDriver.awsLogs(AwsLogDriverProps.builder().logGroup(applicationLogGroup)
                .streamPrefix(serviceProps.getApplicationLogStreamPrefixName()).build());
        return applicationLogDriver;

    }

    private ContainerDefinition appicationContainerDefinition(ServiceProps serviceProps) {
        applicationContainerDefinition = ContainerDefinition.Builder
                .create(this, serviceProps.getApplicationContainerName() + "ContainerDefinition")
                .essential(serviceProps.getApplicationEssentialContainer())
                .containerName(serviceProps.getApplicationContainerName())
                .command(serviceProps.getApplicationContainerCommands())
                .entryPoint(serviceProps.getApplicationEntryPoints()).image(serviceProps.getApplicationContainerImage())
                .cpu(serviceProps.getApplicationContainerCpuLimit())
                .memoryLimitMiB(serviceProps.getApplicationContainermemoryLimit())
                .portMappings(serviceProps.getApplicationContainerPorts()).secrets(serviceProps.getSecret())
                .environmentFiles(serviceProps.getEnvironmentFiles()).logging(applicationLogDriver)
                .taskDefinition(taskDefinition).build();

        return applicationContainerDefinition;
    }

    private LogDriver otelLogDriver(ServiceProps serviceProps) {
        if (serviceProps.getEnableOtelTracing()) {
            otelLogGroup = LogGroup.fromLogGroupName(this, "OtelCollectorLogGroup", serviceProps.getOtelLogGroupName());
            // otelLogGroup.applyRemovalPolicy(serviceProps.getOtelLogGroupRemovalPolicy());

            otelLogDriver = LogDriver.awsLogs(AwsLogDriverProps.builder().logGroup(otelLogGroup)
                    .streamPrefix(serviceProps.getOtelLogStreamPrefixName()).build());
        }

        return otelLogDriver;

    }

    private ContainerDefinition otelContainerDefinition(ServiceProps serviceProps) {
        if (serviceProps.getEnableOtelTracing()) {
            otelContainerDefinition = ContainerDefinition.Builder.create(this, "OtelContainerDefinition")
                    .image(serviceProps.getOtelContainerImage()).containerName(serviceProps.getOtelContainerName())
                    .portMappings(serviceProps.getOtelContainerPorts()).cpu(serviceProps.getOtelContainerCpuLimit())
                    .memoryReservationMiB(serviceProps.getOtelContainermemoryLimit()).logging(otelLogDriver)
                    .environmentFiles(serviceProps.getEnvironmentFiles())
                    .command(serviceProps.getOtelContainerCommands()).essential(true).taskDefinition(taskDefinition)
                    .build();
        }

        return otelContainerDefinition;
    }

    private BaseService service(ServiceProps serviceProps) {
        switch (serviceProps.getServiceType()) {
            case EC2_SERVICE: {
                service = Ec2Service.Builder.create(this, serviceProps.getServiceName() + "EcsService")
                        .cluster(serviceProps.getCluster()).placementStrategies(serviceProps.getPlacementStrategies())
                        .placementConstraints(serviceProps.getPlacementConstraints()).taskDefinition(taskDefinition)
                        .circuitBreaker(DeploymentCircuitBreaker.builder()
                                .rollback(serviceProps.getEnableCircuitBreaker()).build())
                        .minHealthyPercent(serviceProps.getMinimumHealthPercent())
                        .maxHealthyPercent(serviceProps.getMaximumHealthPercent())
                        .capacityProviderStrategies(serviceProps.getCapacityProviderStrategies())
                        .cloudMapOptions(serviceProps.getCloudMapOptions()).serviceName(serviceProps.getServiceName())
                        .desiredCount(serviceProps.getTaskDesiredCount()).vpcSubnets(serviceProps.getSubnetSelection())
                        .securityGroups(serviceProps.getSecurityGroups()).build();
                break;
            }
            case FARGATE_SERVICE: {
                service = FargateService.Builder.create(this, serviceProps.getServiceName() + "EcsService")
                        .cluster(serviceProps.getCluster()).assignPublicIp(serviceProps.isAssignPublicIp())
                        .taskDefinition(taskDefinition).platformVersion(serviceProps.getFargatePlatformVersion())
                        .circuitBreaker(DeploymentCircuitBreaker.builder()
                                .rollback(serviceProps.getEnableCircuitBreaker()).build())
                        .minHealthyPercent(serviceProps.getMinimumHealthPercent())
                        .maxHealthyPercent(serviceProps.getMaximumHealthPercent())
                        .capacityProviderStrategies(serviceProps.getCapacityProviderStrategies())
                        .cloudMapOptions(serviceProps.getCloudMapOptions()).serviceName(serviceProps.getServiceName())
                        .desiredCount(serviceProps.getTaskDesiredCount()).vpcSubnets(serviceProps.getSubnetSelection())
                        .securityGroups(serviceProps.getSecurityGroups()).build();
                break;
            }
            default:
                throw new IllegalArgumentException("Service type must be either Ec2 or Fargate");
        }

        service.applyRemovalPolicy(serviceProps.getServiceRemovalPolicy());
        return service;
    }

    private ApplicationListenerRule createListenerRule(ServiceProps serviceProps) {
        if (serviceProps.getAssignLoadBalancer()) {
            applicationListenerRule =
                    ApplicationListenerRule.Builder.create(this, serviceProps.getServiceName() + "ListenerRule")
                            .listener(serviceProps.getApplicationListener()).action(serviceProps.getListenerAction())
                            .conditions(serviceProps.getListenerConditions())
                            .priority(serviceProps.getListenerRulePriority()).build();
        }

        return applicationListenerRule;

    }

    private void addTargetToTargetGroup(ServiceProps serviceProps) {
        if (serviceProps.getAssignLoadBalancer()) {
            serviceProps.getApplicationTargetGroup()
                    .addTarget(service.loadBalancerTarget(
                            LoadBalancerTargetOptions.builder().protocol(serviceProps.getTargetProtocol())
                                    .containerName(serviceProps.getApplicationContainerName())
                                    .containerPort(serviceProps.getLbTagretContainerPort()).build()));
        }

    }

    private ScalableTaskCount createScalableTaskCount(ServiceProps serviceProps) {
        if (serviceProps.getEnableServiceScaling()) {

            scalableTaskCount = service.autoScaleTaskCount(
                    EnableScalingProps.builder().maxCapacity(serviceProps.getMaximunScalingTaskCount())
                            .minCapacity(serviceProps.getMinimumScalingTaskCount()).build());
            switch (serviceProps.getScalingType()) {
                case MEMEORY_UTILIZATION_SCALING_TYPE: {
                    scalableTaskCount.scaleOnMemoryUtilization(serviceProps.getServiceName() + "ScalingUsingMemory",
                            serviceProps.getMemoryUtilizationScalingProps());
                    break;
                }
                case CPU_UTILIZATION_SCALING_TYPE: {
                    scalableTaskCount.scaleOnCpuUtilization(serviceProps.getServiceName() + "ScalingUsingCpu",
                            serviceProps.getCpuUtilizationScalingProps());
                    break;
                }
                case REQUEST_COUNT_SCALING_TYPE: {
                    scalableTaskCount.scaleOnRequestCount(serviceProps.getServiceName() + "ScalingUsingCpu",
                            serviceProps.getRequestCountScalingProps());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Given scaling type not found");
            }

        }

        return scalableTaskCount;
    }

}
