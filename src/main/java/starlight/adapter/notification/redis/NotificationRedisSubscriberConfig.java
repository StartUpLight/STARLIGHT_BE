package starlight.adapter.notification.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class NotificationRedisSubscriberConfig {

    @Bean
    public ChannelTopic notificationChannelTopic(
            @Value("${notification.redis.topic:notification:events}") String topic
    ) {
        return new ChannelTopic(topic);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisNotificationSubscriber redisNotificationSubscriber,
            ChannelTopic notificationChannelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisNotificationSubscriber, notificationChannelTopic);
        return container;
    }
}
