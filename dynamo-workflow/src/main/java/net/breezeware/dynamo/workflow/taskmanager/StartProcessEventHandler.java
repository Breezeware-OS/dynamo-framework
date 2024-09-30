package net.breezeware.dynamo.workflow.taskmanager;

public interface StartProcessEventHandler {
    Object initiateNewProcessInstance(String processId);
}