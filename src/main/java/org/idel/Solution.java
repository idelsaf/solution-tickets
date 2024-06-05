package org.idel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

public class Solution {
    public static void main(String[] args) {
        List<Ticket> tickets = parse();

        System.out.println("Минимальное время полета между городами" +
                "Владивосток и Тель-Авив для каждого" +
                "авиаперевозчика:");
        Map<String, Long> minFlightTime = getMinFlightTime(tickets);
        for (String key : minFlightTime.keySet()) {
            long hours = minFlightTime.get(key) / 60;
            long minutes = minFlightTime.get(key) % 60;
            System.out.println(key + ": " + hours + ":" + minutes);
        }

        System.out.println("\nРазница между средней ценой и медианой для" +
                "полета между городами  Владивосток и Тель-Авив:");
        System.out.println(getDiffBetweenPrices(tickets));
    }

    public static List<Ticket> parse() {
        List<Ticket> tickets = new ArrayList<>();

        try (FileReader fileReader = new FileReader("tickets.json")) {
            Type listOfTicketType = new TypeToken<ArrayList<Ticket>>() {}.getType();
            JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
            Gson gson = new Gson();
            tickets = gson.fromJson(jsonObject.get("tickets"), listOfTicketType);
        } catch (IOException e) {
            System.out.println("Parsing error");
        }

        return tickets;
    }

    public static Map<String, Long> getMinFlightTime(List<Ticket> tickets) {
        Map<String, Long> minFlightTime = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        for (Ticket ticket : tickets) {
            if (!ticket.getOrigin_name().equals("Владивосток") || !ticket.getDestination_name().equals("Тель-Авив")) {
                continue;
            }

            String carrier = ticket.getCarrier();

            LocalDate departureDate = LocalDate.parse(ticket.getDeparture_date(), dateFormatter);
            LocalTime departureTime = LocalTime.parse(ticket.getDeparture_time(), timeFormatter);
            LocalDate arrivalDate = LocalDate.parse(ticket.getArrival_date(), dateFormatter);
            LocalTime arrivalTime = LocalTime.parse(ticket.getArrival_time(), timeFormatter);

            LocalDateTime departureDateTime = LocalDateTime.of(departureDate, departureTime);
            LocalDateTime arrivalDateTime = LocalDateTime.of(arrivalDate, arrivalTime);

            Duration duration = Duration.between(departureDateTime, arrivalDateTime);
            long durationInMinutes = duration.toMinutes();

            if (!minFlightTime.containsKey(carrier) || (minFlightTime.get(carrier) > durationInMinutes)) {
                minFlightTime.put(carrier, durationInMinutes);
            }
        }

        return minFlightTime;
    }

    public static double getDiffBetweenPrices(List<Ticket> tickets) {
        List<Integer> prices = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if (ticket.getOrigin_name().equals("Владивосток") && ticket.getDestination_name().equals("Тель-Авив")) {
                prices.add(ticket.getPrice());
            }
        }

        double avgPrice = prices.stream()
                .mapToInt(Integer::intValue)
                .average()
                .getAsDouble();

        IntStream sortedPrices = prices.stream()
                .mapToInt(Integer::intValue)
                .sorted();

        int pricesSize = prices.size();
        double medianPrice = pricesSize % 2 == 0 ?
                sortedPrices.skip(pricesSize / 2 - 1).limit(2).average().getAsDouble() :
                sortedPrices.skip(pricesSize / 2).findFirst().getAsInt();

        return avgPrice - medianPrice;
    }
}

@Getter
@Setter
class Ticket {
    private String origin;
    private String origin_name;
    private String destination;
    private String destination_name;
    private String departure_date;
    private String departure_time;
    private String arrival_date;
    private String arrival_time;
    private String carrier;
    private int stops;
    private int price;

    @Override
    public String toString() {
        return origin_name + " " + destination_name + " " + carrier + " " + departure_time + " " + arrival_time + " " + price;
    }
}