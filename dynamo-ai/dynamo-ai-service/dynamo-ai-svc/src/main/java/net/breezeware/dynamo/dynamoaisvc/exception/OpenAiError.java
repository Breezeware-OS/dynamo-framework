package net.breezeware.dynamo.dynamoaisvc.exception;

public record OpenAiError(String message, String type, String param, String code) {
}
