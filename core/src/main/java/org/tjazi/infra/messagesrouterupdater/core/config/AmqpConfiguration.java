package org.tjazi.infra.messagesrouterupdater.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tjazi.infra.messagesrouterupdater.core.endpoints.queuehandlers.MessagesRouteUpdaterEndpoint;

/**
 * Created by Krzysztof Wasiak on 22/01/2016.
 */

@Configuration
public class AmqpConfiguration {

    @Value("${routerupdater.inputqueuename}")
    private String queueName;

    @Value("${routerupdater.exchangename}")
    private String exchangeName;

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }

    @Bean
    MessageConverter messageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter(MessageConverter messageConverter, MessagesRouteUpdaterEndpoint routeUpdaterEndpoint) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(routeUpdaterEndpoint, messageConverter);
        messageListenerAdapter.setDefaultListenerMethod("updateRoute");
        return messageListenerAdapter;
    }

    @Bean
    SimpleMessageListenerContainer container(
            ConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);

        container.setMessageListener(messageListenerAdapter);

        return container;
    }

    @Bean
    MessagesRouteUpdaterEndpoint routeUpdaterEndpoint() {
        return new MessagesRouteUpdaterEndpoint();
    }

}
