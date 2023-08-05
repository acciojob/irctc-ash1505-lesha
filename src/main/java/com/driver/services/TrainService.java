package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();

        List<Station> stationList = trainEntryDto.getStationRoute();
        String rout = "";
        for(Station s: stationList){
            rout += s.toString() + ",";
        }
        train.setRoute(rout);

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        train = trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.


        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        Station from = seatAvailabilityEntryDto.getFromStation();
        Station to = seatAvailabilityEntryDto.getToStation();
        String rout = train.getRoute();

        List<Ticket> ticketList = train.getBookedTickets();
        List<Station> stationList = new ArrayList<>();
        for(String s: rout.split(",")){
            stationList.add(Enum.valueOf(Station.class, s));
        }

        int si = stationList.indexOf(from);
        int ei = stationList.indexOf(to);

        int avai = train.getNoOfSeats();
        for(Ticket t: ticketList){
            Station frm_st = t.getFromStation();
            Station to_st = t.getToStation();
            int start = stationList.indexOf(frm_st);
            int end = stationList.indexOf(to_st);

            if((start >= si && start < ei) || (end > si && end <= ei) || (start <= si && end >= ei)){
                avai -= t.getPassengersList().size();
            }
        }
        return avai;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        String rout = train.getRoute();
        List<Station> stationList = new ArrayList<>();
        int res = 0;
        for(String s: rout.split(",")){
            stationList.add(Enum.valueOf(Station.class, s));
        }
        if(stationList.contains(station)){
            List<Ticket> ticketList = train.getBookedTickets();
            for(Ticket t: ticketList){
                if(t.getFromStation().equals(station)){
                    res += t.getPassengersList().size();
                }
            }
        }else {
            throw new Exception();
        }

        return res;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int res = 0;
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket t: ticketList){
            List<Passenger> passengerList = t.getPassengersList();
            for(Passenger p: passengerList){
                res = Math.max(0, p.getAge());
            }
        }
        return res;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trainList = trainRepository.findAll();
        List<Integer> res = new ArrayList<>();
        for(Train train: trainList){
            String rout = train.getRoute();
            LocalTime dt = train.getDepartureTime();
            List<Station> stationList = new ArrayList<>();
            for(String s: rout.split(",")){
                stationList.add(Enum.valueOf(Station.class, s));
            }
            if(stationList.contains(station)){
                LocalTime arrivalTime;
                int idx = stationList.indexOf(station);
                arrivalTime = dt.plusHours(idx);

                if(arrivalTime.isAfter(startTime) && arrivalTime.isBefore(endTime))
                    res.add(train.getTrainId());
            }
        }
        return res;
    }

}
