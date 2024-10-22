package guru.springframework.msscssm;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import reactor.core.publisher.Mono;

@SpringBootTest
public class StateMachieConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void TestNewStateMachine() {

        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());

        sm.startReactively().subscribe();
        System.out.println("**START**" + sm.getState().toString());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE).build()))
            .subscribe();
        System.out.println("**PRE_AUTH**" + sm.getState().toString());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).build()))
            .subscribe();
        System.out.println("**PRE_AUTH_APPROVED**" +sm.getState().toString());

    }
}
