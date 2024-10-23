package guru.springframework.msscssm.services;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService{

    public static final String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return null;

    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {

        sm.sendEvent(Mono.just(
                        MessageBuilder
                            .withPayload(event)
                            .setHeader(PAYMENT_ID_HEADER, paymentId)
                            .build()))
                    .subscribe();
    }

    // populate state machine from database payment state

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){

        Payment payment = paymentRepository.getReferenceById(paymentId);

        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        sm.stopReactively();
        sm.getStateMachineAccessor()
            .doWithAllRegions(sma -> {
                sma.resetStateMachineReactively(new
                    DefaultStateMachineContext<PaymentState,PaymentEvent>(payment.getState(), null, null, null));
            });
        sm.startReactively();

        return sm;
    }

}
