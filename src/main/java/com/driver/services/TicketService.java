package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Ticket ticket = new Ticket();
        Optional<Train> optionalTrain = trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!optionalTrain.isPresent())
            throw new Exception();

        Train train = optionalTrain.get();
        if(bookTicketEntryDto.getNoOfSeats() > trainService.calculateAvailableSeats(new SeatAvailabilityEntryDto(train.getTrainId(), bookTicketEntryDto.getFromStation(), bookTicketEntryDto.getToStation()))){
            throw new Exception();
        }

        String rout = train.getRoute();
        HashSet<String> stationSet = new HashSet<>(Arrays.asList(rout.split(",")));

        if(!stationSet.contains(bookTicketEntryDto.getFromStation()) || !stationSet.contains(bookTicketEntryDto.getToStation()))
            throw new Exception();

        List<String> stList = Arrays.asList(rout.split(","));
        int si = stList.indexOf(bookTicketEntryDto.getFromStation());
        int ei = stList.indexOf(bookTicketEntryDto.getToStation());
        int fare = 300 * (ei-si) * bookTicketEntryDto.getNoOfSeats();

        List<Passenger> passengerList = new ArrayList<>();
        for(int id: bookTicketEntryDto.getPassengerIds()){
            Passenger p = passengerRepository.findById(id).get();
            passengerList.add(p);
        }
        ticket.setPassengersList(passengerList);

        ticket.setTrain(train);

        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        ticket.setTotalFare(fare);

        ticket = ticketRepository.save(ticket);

        train.getBookedTickets().add(ticket);
        passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get().getBookedTickets().add(ticket);

        return ticket.getTicketId();
    }
}
