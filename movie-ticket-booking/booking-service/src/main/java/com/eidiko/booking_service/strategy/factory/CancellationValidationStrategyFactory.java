package com.eidiko.booking_service.strategy.factory;

import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.strategy.validation.AllSeatsCanceledValidationStrategy;
import com.eidiko.booking_service.strategy.validation.CancellationValidationStrategy;
import com.eidiko.booking_service.strategy.validation.RequestedSeatsCanceledValidationStrategy;
import com.eidiko.booking_service.strategy.validation.TimeCancellationValidationStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CancellationValidationStrategyFactory {

    public List<CancellationValidationStrategy> getStrategies(Booking booking, Set<String> requestedSeats) {
        List<CancellationValidationStrategy> strategies = new ArrayList<>();
        strategies.add(new TimeCancellationValidationStrategy());
        strategies.add(new AllSeatsCanceledValidationStrategy());
        strategies.add(new RequestedSeatsCanceledValidationStrategy(requestedSeats));
        return strategies;
    }
}
