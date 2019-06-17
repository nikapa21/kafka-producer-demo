import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.peer.ListPeer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckAlertDemo {


    String addr;
    int port;

    List<Topic> topics = new ArrayList<>();

    List<Value> publisherValues = new ArrayList<>();
    List<BusLine> publisherBusLines = new ArrayList<>();
    List<RouteCode> publisherRouteCodes = new ArrayList<>();
    List<BusPosition> publisherBusPositions = new ArrayList<>();

    public CheckAlertDemo(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(CheckAlertDemo.class.getName());

        //find all the vehicle ids in one set
        String busPositionFile = args[0];

        String busPositionsFile = "./Dataset/DS_project_dataset/"+busPositionFile;

        List<Record> publisherBusRecords = new ArrayList<>();
        Map<String, Set<Coordinate>> rawCoordinatesPerTopic = new HashMap<>();

        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(busPositionsFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                Record record = new Record(fields[0], fields[1], fields[2], Double.parseDouble(fields[3]), Double.parseDouble(fields[4]), fields[5]);
                return record; })
                    .forEach(busPositionline -> publisherBusRecords.add(busPositionline));

        } catch(IOException e) {
            e.printStackTrace();
        }

        for(Record record : publisherBusRecords) {
            double x = record.getLatitude();
            double y = record.getLongtitude();

            Coordinate coordinate = new Coordinate(x, y);

            Set<Coordinate> raw = rawCoordinatesPerTopic.get(record.getRouteCode());
            if (raw == null) {
                raw = new HashSet<>();
                raw.add(coordinate);
                rawCoordinatesPerTopic.put(record.getRouteCode(), raw);
            }
            else {
                raw.add(coordinate);
            }
        }

        String alertFile = "./Dataset/DS_project_dataset/busPositionsMix10false8181799.txt";

        List<Record> alertFileRecords = new ArrayList<>();
        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(alertFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                Record record = new Record(fields[0], fields[1], fields[2], Double.parseDouble(fields[3]), Double.parseDouble(fields[4]), fields[5]);
                return record; })
                    .forEach(busPositionline -> alertFileRecords.add(busPositionline));

        } catch(IOException e) {
            e.printStackTrace();
        }

        int samplesOnRoute = 0;
        int alertCounter = 0;

        for(Record record : alertFileRecords) {
            double x = record.getLatitude();
            double y = record.getLongtitude();

            Coordinate coordinate = new Coordinate(x, y);

            if (rawCoordinatesPerTopic.get(record.getRouteCode()) == null) {
                System.out.println("pigame na paroume sampled gia to topic kati pou den exoume raw ");
            }
            else {
                boolean coordinateIsOnCorrectRoute = rawCoordinatesPerTopic.get(record.getRouteCode()).stream().anyMatch(c -> c.equals(coordinate));

                if (coordinateIsOnCorrectRoute) {
//                    logger.info("All good keep receiving sampled data. So far " + ++samplesOnRoute);
                } else {
                    logger.info("###############");
                    logger.info("ALERT found. Alert count is " + ++alertCounter);
                }
            }

        }




    }
}