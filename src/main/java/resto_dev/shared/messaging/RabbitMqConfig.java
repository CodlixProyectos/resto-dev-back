package resto_dev.shared.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el proyecto Codlix.
 * Aquí definimos las colas, exchanges y bindings necesarios para la mensajería asíncrona.
 */
@Configuration
public class RabbitMqConfig {

    // Nombres de las colas y exchanges
    public static final String TENANT_PROVISIONING_QUEUE = "tenant.provisioning.queue";
    public static final String TENANT_EXCHANGE = "tenant.exchange";
    public static final String TENANT_PROVISIONING_ROUTING_KEY = "tenant.provisioning.key";

    /**
     * Definición de la cola donde se encolarán las solicitudes de creación de esquema.
     * durable = true para que los mensajes no se pierdan si RabbitMQ se reinicia.
     */
    @Bean
    public Queue provisioningQueue() {
        return new Queue(TENANT_PROVISIONING_QUEUE, true);
    }

    /**
     * El DirectExchange dirige los mensajes a colas específicas basándose en una "routing key".
     */
    @Bean
    public DirectExchange tenantExchange() {
        return new DirectExchange(TENANT_EXCHANGE);
    }

    /**
     * El Binding une la cola con el exchange usando la clave de enrutamiento.
     */
    @Bean
    public Binding provisioningBinding(Queue provisioningQueue, DirectExchange tenantExchange) {
        return BindingBuilder.bind(provisioningQueue).to(tenantExchange).with(TENANT_PROVISIONING_ROUTING_KEY);
    }

    /**
     * Configuramos Jackson para convertir automáticamente nuestros objetos (DTOs) a JSON.
     * Esto evita usar la serialización nativa de Java, lo cual es más estándar y seguro.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
