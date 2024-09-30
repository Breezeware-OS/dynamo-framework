package net.breezeware.dynamo.iaac.entity;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ecs.CapacityProviderStrategy;
import software.amazon.awscdk.services.ecs.CloudMapOptions;
import software.amazon.awscdk.services.ecs.Compatibility;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.EnvironmentFile;
import software.amazon.awscdk.services.ecs.FargatePlatformVersion;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.NetworkMode;
import software.amazon.awscdk.services.ecs.PlacementConstraint;
import software.amazon.awscdk.services.ecs.PlacementStrategy;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.RequestCountScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.Secret;
import software.amazon.awscdk.services.ecs.Volume;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerRule;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationTargetGroup;
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerAction;
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerCondition;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.logs.RetentionDays;

@Data
@Builder
public class ServiceProps {

    /**
     * Task Definition Name.
     */
    private String taskDefinitionName;

    /**
     * Execution Role.
     */
    private IRole executionRole;

    /**
     * Task Role.
     */
    private IRole taskRole;

    /**
     * Cpu Limit.
     */
    private String cpu;

    /**
     * Memory Limit.
     */
    private String memoryMiB;

    /**
     * Network Mode.
     */
    private NetworkMode networkMode;

    /**
     * Compatibility.
     */
    private Compatibility compatibility;

    /**
     * Task Definition RemovalPolicy.
     */
    private RemovalPolicy taskDefinitionRemovalPolicy;

    /**
     * Name of the Log Group.
     */
    private String applicationLogGroupName;

    /**
     * Log Stream Prefix Name.
     */
    private String applicationLogStreamPrefixName;

    /**
     * Log Group Retention Days.
     */
    private RetentionDays applicationRetentionDays;

    /**
     * Log Group Removal Policy.
     */
    private RemovalPolicy applicationLogGroupRemovalPolicy;

    /**
     * Application Image.
     */
    private ContainerImage applicationContainerImage;

    /**
     * Application Container Name.
     */
    private String applicationContainerName;

    /**
     * Application Container Port.
     */
    private List<PortMapping> applicationContainerPorts;

    /**
     * Cpu limit for application container inside the task.
     */
    private Number applicationContainerCpuLimit;

    /**
     * Memory limit for container inside the task.
     */
    private Number applicationContainermemoryLimit;

    /**
     * Essential Container.
     */
    private Boolean applicationEssentialContainer;

    /**
     * Volumes for TaskDefinition.
     */
    private List<Volume> volumes;

    /**
     * Secrets used for container.
     */
    private Map<String, Secret> secret;

    private List<EnvironmentFile> environmentFiles;

    /**
     * Enable Otel to retrive traces from application.
     */
    private Boolean enableOtelTracing;

    /**
     * Name of the Otel Log Group.
     */
    private String otelLogGroupName;

    /**
     * Otel Log Stream Prefix Name.
     */
    private String otelLogStreamPrefixName;

    /**
     * Log Group Removal Policy.
     */
    private RemovalPolicy otelLogGroupRemovalPolicy;

    /**
     * Application Image.
     */
    private ContainerImage otelContainerImage;

    /**
     * Otel Container Name.
     */
    private String otelContainerName;

    /**
     * Otel Container Port.
     */
    private List<PortMapping> otelContainerPorts;

    /**
     * Cpu limit for container inside the task.
     */
    private Number otelContainerCpuLimit;

    /**
     * Memory limit for container inside the task.
     */
    private Number otelContainermemoryLimit;

    /**
     * Essential Container.
     */
    private Boolean otelEssentialContainer;

    /**
     * Log Group Retention Days.
     */
    private RetentionDays otelRetentionDays;

    /**
     * Application EntryPoint command.
     */
    private List<String> applicationEntryPoints;

    /**
     * Application Container command.
     */
    private List<String> applicationContainerCommands;

    /**
     * Otel Container command.
     */
    private List<String> otelContainerCommands;

    /**
     * ECS Cluster.
     */
    private ICluster cluster;
    /**
     * Service type of the ECS Cluster.
     */
    private ServiceType serviceType;
    /**
     * Name of the ECS Service.
     */
    private String serviceName;
    /**
     * Options to enabling AWS Cloud Map for an Amazon ECS service.
     */
    private CloudMapOptions cloudMapOptions;
    /**
     * Options to enabling AWS Cloud Map for an Amazon ECS service.
     */
    private FargatePlatformVersion fargatePlatformVersion;
    /**
     * Ecs service security groups.
     */
    private List<ISecurityGroup> securityGroups;
    /**
     * Placement strategies to use for tasks in the service.
     */
    private List<PlacementStrategy> placementStrategies;
    /*
     * Placement constraints to use for tasks in the service.
     */
    private List<PlacementConstraint> placementConstraints;
    /*
     * Public Ip for tasks.
     */
    private boolean assignPublicIp;
    /*
     * Subnets to associate with the service.
     */
    private SubnetSelection subnetSelection;
    /*
     * Desired task count.
     */
    private Number taskDesiredCount;
    /*
     * Capacity Provider for Ecs.
     */
    private List<CapacityProviderStrategy> capacityProviderStrategies;
    /**
     * Ecs Service Removal Policy.
     */
    private RemovalPolicy serviceRemovalPolicy;
    /**
     * Assign Loadbalancer.
     */
    private Boolean assignLoadBalancer;
    /**
     * Application Listener Configuration.
     */
    private IApplicationListener applicationListener;
    /**
     * Application Listener Rule Configuration.
     */
    private ApplicationListenerRule applicationListenerRule;
    /**
     * Application Target Group.
     */
    private IApplicationTargetGroup applicationTargetGroup;
    /**
     * Application listener Action.
     */
    private ListenerAction listenerAction;
    /**
     * Application listener rule conditions.
     */
    private List<ListenerCondition> listenerConditions;
    /**
     * Application listener rule priority.
     */
    private Number listenerRulePriority;
    /**
     * Application target protocol.
     */
    private Protocol targetProtocol;
    /**
     * Container Port for Loadbalancer.
     */
    private Number lbTagretContainerPort;
    /**
     * Enable service scaling.
     */
    private Boolean enableServiceScaling;
    /**
     * Scalable Task count.
     */
    private ScalableTaskCount scalableTaskCount;
    /**
     * Minimum scalable Task count.
     */
    private Number minimumScalingTaskCount;
    /**
     * Maximum scalable Task count.
     */
    private Number maximunScalingTaskCount;
    /**
     * Scaling type.
     */
    private ScalingType scalingType;
    /**
     * Memory Utilization Scaling Props.
     */
    private MemoryUtilizationScalingProps memoryUtilizationScalingProps;
    /**
     * Cpu Utilization Scaling Props.
     */
    private CpuUtilizationScalingProps cpuUtilizationScalingProps;
    /**
     * Request Utilization Scaling Props.
     */
    private RequestCountScalingProps requestCountScalingProps;
    /**
     * Enable Circuit Breaker.
     */
    private Boolean enableCircuitBreaker;
    /**
     * Deployment Minimum Health Percent.
     */
    private Number minimumHealthPercent;
    /**
     * Deployment Maximum Health Percent.
     */
    private Number maximumHealthPercent;

    /**
     * ECS Service Type.
     */
    public enum ServiceType {
        EC2_SERVICE("Ec2"), FARGATE_SERVICE("Fargate");

        @Getter
        private String serviceType;

        ServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

    }

    public enum ScalingType {
        MEMEORY_UTILIZATION_SCALING_TYPE("memory_utilization"), CPU_UTILIZATION_SCALING_TYPE("cpu_utilization"),
        REQUEST_COUNT_SCALING_TYPE("request_count");

        @Getter
        private String scalingType;

        ScalingType(String scalingType) {
            this.scalingType = scalingType;
        }

    }

}
