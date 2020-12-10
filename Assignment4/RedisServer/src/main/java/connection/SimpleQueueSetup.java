package connection;

import java.util.List;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;


public class SimpleQueueSetup {
  private final static String QUEUE_NAME = "ikkyoneQueue";

//  final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//  String queueUrl = "https://sqs.us-west-2.amazonaws.com/417514485731/IkkyoneQueue";

  private final static SdkHttpClient sdkHttpClient = ApacheHttpClient.builder()
      .maxConnections(800)
      .build();

  private final static SqsClient sqsClient = SqsClient.builder()
      .httpClient(sdkHttpClient)
      .build();

  private static String queueUrl = "https://sqs.us-west-2.amazonaws.com/417514485731/IkkyoneQueue";

  private static String getQueueUrl() {
    GetQueueUrlRequest request = GetQueueUrlRequest.builder()
        .queueName(QUEUE_NAME)
        .build();

    GetQueueUrlResponse response = sqsClient.getQueueUrl(request);

    if (response.queueUrl() != null) {
      return response.queueUrl();
    }

    throw new IllegalArgumentException(String.format("Failed to retrieve queue url for queue: %s", QUEUE_NAME));
  }


  public static void sendMsgToSQS(String messageBody) {
    if (queueUrl == null) {
      queueUrl = getQueueUrl();
    }

    SendMessageRequest request = SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(messageBody)
        .build();

    try {
      sqsClient.sendMessage(request);
    } catch (SqsException e) {
      throw new RuntimeException(String.format("Failed to send message %s to queue %s",
          messageBody,
          QUEUE_NAME), e);
    }
  }

  public static List<Message> getMsgFromSQS() {
    if (queueUrl == null) {
      queueUrl = getQueueUrl();
    }

    ReceiveMessageRequest request = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .build();

    try {
      return sqsClient.receiveMessage(request).messages();
    } catch (SqsException e) {
      throw new RuntimeException(String.format("Failed to get messages from queue %s",
          QUEUE_NAME));
    }
  }

  public static void deleteMsgInSQS(
       String msgReceiptHandle) {
    if (queueUrl == null) {
      queueUrl = getQueueUrl();
    }

    DeleteMessageRequest request = DeleteMessageRequest.builder()
        .queueUrl(queueUrl)
        .receiptHandle(msgReceiptHandle)
        .build();

    try {
      sqsClient.deleteMessage(request);
    } catch (SqsException e ) {
      throw new RuntimeException(String.format("Failed to delete message %s from queue %s",
          msgReceiptHandle,
          QUEUE_NAME));
    }
  }
}
