package dev.jpje.jobtracker.server;

import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sns.core.TopicMessageChannel;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.QueueAttributes;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AwsRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    hints.reflection().registerType(SnsTemplate.class, MemberCategory.values());
    hints.reflection().registerType(TopicMessageChannel.class, MemberCategory.values());
    hints.reflection().registerType(SqsTemplate.class, MemberCategory.values());
    hints.reflection().registerType(QueueAttributes.class, MemberCategory.values());
    hints.reflection().registerType(SnsClient.class, MemberCategory.values());
    hints.reflection().registerType(SqsClient.class, MemberCategory.values());
    hints.reflection().registerType(SqsListener.class, MemberCategory.values());
  }
}
