package guru.springframework.msscssm.config.actions;

import java.util.Random;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import reactor.core.publisher.Mono;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        System.out.println("Auth was called!!!");

        if (new Random().nextInt(10) < 8) {
            System.out.println("Approved");
            context.getStateMachine().sendEvent(Mono.just(
                    MessageBuilder
                            .withPayload(PaymentEvent.AUTH_APPROVED)
                            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                    context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                            .build()))
                    .subscribe();
        } else {
            System.out.println("Declined");
            context.getStateMachine().sendEvent(Mono.just(
                    MessageBuilder
                            .withPayload(PaymentEvent.AUTH_DECLINED)
                            .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                    context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                            .build()))
                    .subscribe();
        }
    }
}
