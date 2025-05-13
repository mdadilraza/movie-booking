package com.eidiko.booking_service.strategy.factory;

import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.strategy.validation.AllSeatsCanceledValidationStrategy;
import com.eidiko.booking_service.strategy.validation.CancellationValidationStrategy;
import com.eidiko.booking_service.strategy.validation.RequestedSeatsCanceledValidationStrategy;
import com.eidiko.booking_service.strategy.validation.TimeCancellationValidationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CancellationValidationStrategyFactory {
    private final TimeCancellationValidationStrategy timeStrategy;
    private final AllSeatsCanceledValidationStrategy allSeatsStrategy;

    public List<CancellationValidationStrategy> getStrategies(Booking booking, Set<String> requestedSeats) {
        List<CancellationValidationStrategy> strategies = new ArrayList<>();
        strategies.add(timeStrategy);
        strategies.add(allSeatsStrategy);
        strategies.add(new RequestedSeatsCanceledValidationStrategy(requestedSeats));
        return strategies;
    }
}
