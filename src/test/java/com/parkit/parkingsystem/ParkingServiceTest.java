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
import org.junit.jupiter.api.function.Executable;
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
    public void processExitingVehicleCarTest() throws Exception {
        System.out.println("processExitingVehicleCarTest TEST CAR");
        double ticketPrice = 1.5;
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(ticket.getId());
        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        parkingSpotDAO.updateParking(parkingSpot);
        parkingService.processExitingVehicle();
        verify(ticketDAO).getNbTicket("ABCDEF");
        Assertions.assertEquals(ticketPrice, ticket.getPrice());
    }

    @Test
    public void processExitingVehiculeBikeTest() throws Exception {
        System.out.println("processExitingVehicleBikeTest TEST BIKE");
        double ticketPrice = 0.95;
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("GHIJKL");
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(2);
        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        parkingSpotDAO.updateParking(parkingSpot);
        parkingService.processExitingVehicle();
        verify(ticketDAO).getNbTicket("GHIJKL");
        Assertions.assertEquals(ticketPrice, ticket.getPrice());
    }

    @Test
    public void processIncomingVehicleCarTest() {
        System.out.println("processIncomingVehiculeCarTest TEST CAR");
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        parkingService.processIncomingVehicle();
        Assertions.assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
    }

    @Test
    public void processIncomingVehiculeBikeTest() {
        System.out.println("processIncomingVehiculeBikeTest TEST BIKE");
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
        when(ticketDAO.getNbTicket("GHIJKL")).thenReturn(1);
        parkingService.processIncomingVehicle();
        Assertions.assertEquals(1, ticketDAO.getNbTicket("GHIJKL"));
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
        ticket.setInTime(new Date());
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
    public void getNextParkingNumberIfAvailableCarTest() {
        System.out.println("getNextParkingNumberIfAvailableCarTest TEST CAR");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        parkingService.getNextParkingNumberIfAvailable();
        Assertions.assertTrue(parkingService.getNextParkingNumberIfAvailable().isAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableBikeTest() {
        System.out.println("getNextParkingNumberIfAvailableBikeTest TEST BIKE");
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
        parkingService.getNextParkingNumberIfAvailable();
        Assertions.assertTrue(parkingService.getNextParkingNumberIfAvailable().isAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
        System.out.println("getNextParkingNumberIfAvailableParkingNumberNotFoundTest TEST");
        parkingService.getNextParkingNumberIfAvailable();
        Assertions.assertNull(parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberWrongArgumentTest() {
        System.out.println("getNextParkingNumberIfAvailableParkingNumberWrongArgumentTest TEST");
        when(inputReaderUtil.readSelection()).thenReturn(3);
        parkingService.getNextParkingNumberIfAvailable();
        Assertions.assertThrows(Exception.class, (Executable) parkingService.getNextParkingNumberIfAvailable(), "Error fetching parking number from DB. Parking slots might be full");
    }

}


