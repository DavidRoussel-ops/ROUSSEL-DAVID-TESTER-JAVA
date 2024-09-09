package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static FareCalculatorService fareCalculatorService;

    private static Instant startTime;

    @BeforeAll
    public static void initStartingTime() {
        System.out.println("Appel avant le lançement des tests");
        startTime = Instant.now();
    }

    @AfterAll
    public static void endTestDuration() {
        System.out.println("Fin des tests");
        Instant endTime = Instant.now();
        long duration = Duration.between(startTime, endTime).toMillis();
        System.out.println(MessageFormat.format("Durée des tests : {0} ms", duration));
    }

    @BeforeEach
    public void setUpPerTest() {
        try {
            inputReaderUtil.readSelection();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            ParkingType parkingType = ParkingType.CAR;
            parkingSpot.setParkingType(parkingType);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        System.out.println("processExitingVehicleTest TEST ");
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setOutTime(new Date());
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(ticket.getId());
        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        parkingSpotDAO.updateParking(parkingSpot);
        parkingService.processExitingVehicle();
        verify(ticketDAO).getNbTicket("ABCDEF");
    }

    @Test
    public void processIncomingVehicleTest() {
        System.out.println("processIncomingVehiculeTest TEST");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        parkingService.processIncomingVehicle();
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        System.out.println("processIncomingVehiculeTest TEST");
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setOutTime(new Date());
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);
        when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(ticket.getId());
        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        parkingSpotDAO.updateParking(parkingSpot);
        parkingService.processExitingVehicle();
        Assertions.assertFalse(ticketDAO.updateTicket(ticket));
    }

    @Test
    public void getNextParkingNumberIfAvailableTest() {
        System.out.println("getNextParkingNumberIfAvailableTest TEST");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        parkingService.getNextParkingNumberIfAvailable();
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
        System.out.println("getNextParkingNumberIfAvailableParkingNumberNotFoundTest TEST");
        when(inputReaderUtil.readSelection()).thenReturn(0);
        parkingService.getNextParkingNumberIfAvailable();
        Assertions.assertNull(parkingService.getNextParkingNumberIfAvailable());
    }




}


