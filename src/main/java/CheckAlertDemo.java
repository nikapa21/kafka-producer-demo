import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.peer.ListPeer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        List<Value> publisherBusRecords = new ArrayList<>();
        Map<String, List<BusPosition>> rawCoordinatesPerTopic = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM  d yyyy hh:mm:ss:000a");

        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(busPositionsFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                Value value = new Value(fields[1], fields[2], fields[3], "", fields[0], fields[6], Double.parseDouble(fields[4]), Double.parseDouble(fields[5]));
                return value; })
                    .forEach(busPositionline -> publisherBusRecords.add(busPositionline));

        } catch(IOException e) {
            e.printStackTrace();
        }

        for(Value value : publisherBusRecords) {
            double x = value.getLatitude();
            double y = value.getLongtitude();

            BusPosition busPosition = new BusPosition(value.getLineNumber(), value.getRouteCode(), value.getVehicleId(), x, y, value.getInfo());

            List<BusPosition> raw = rawCoordinatesPerTopic.get(value.getBuslineId());
            if (raw == null) {
                raw = new ArrayList<>();
                raw.add(busPosition);
                rawCoordinatesPerTopic.put(value.getBuslineId(), raw);
            }
            else {
                raw.add(busPosition);
            }
        }

        String alertFile = "./Dataset/DS_project_dataset/busAlertWithTopic.txt";

        List<Value> alertFileRecords = new ArrayList<>();
        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(alertFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                Value value = new Value(fields[1], fields[2], fields[3], "", fields[0], fields[6], Double.parseDouble(fields[4]), Double.parseDouble(fields[5]));
                return value; })
                    .forEach(busPositionline -> alertFileRecords.add(busPositionline));

        } catch(IOException e) {
            e.printStackTrace();
        }

        int alertCounter = 0;

        for(Value value : alertFileRecords) {
            double x = value.getLatitude();
            double y = value.getLongtitude();

            BusPosition busPosition = new BusPosition(value.getLineNumber(), value.getRouteCode(), value.getVehicleId(), x, y, value.getInfo());
            boolean coordinateIsOnRoute = false;

            if (rawCoordinatesPerTopic.get(value.getBuslineId()) == null) {
                System.out.println("pigame na paroume sampled gia to topic kati pou den exoume raw ");
            }

            else {

                List<BusPosition> temporaryList = rawCoordinatesPerTopic.get(value.getBuslineId());

                for(int index=0;index<temporaryList.size();index++) { // TODO refactor se while. An ginei refactor, de xreiazetai to teleutaio if if (coordinateIsOnRoute) break;

                    BusPosition currentLocation = temporaryList.get(index);
                    if (busPosition.isInTheVicinity(currentLocation)) {

                        coordinateIsOnRoute = true;
                        try {
                            for (int i = index + 1; i < rawCoordinatesPerTopic.get(value.getBuslineId()).size(); i++) {
                                BusPosition nextLocation = temporaryList.get(i);
                                if (nextLocation.getRouteCode().equals(busPosition.getRouteCode())) {

                                    Date firstParsedDate = null;
                                    try {
                                        firstParsedDate = dateFormat.parse(currentLocation.getTimeStampOfBusPosition().replaceAll("'", "").replaceAll(" info=", ""));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    Date secondParsedDate = null;
                                    try {
                                        secondParsedDate = dateFormat.parse(nextLocation.getTimeStampOfBusPosition().replaceAll("'", "").replaceAll(" info=", ""));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    int diff = Math.abs((int) secondParsedDate.getTime() - (int) firstParsedDate.getTime());

                                    System.out.println("Current Location: " + currentLocation);
                                    System.out.println("Next Location: " + nextLocation);
                                    System.out.println("Ypologizoume oti tha einai stin epomeni stasi to polu se " + diff/1000 + " seconds!");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Current Location: " + currentLocation);
                            System.out.println("Next Location: " + currentLocation);
                            System.out.println("Ftasame sto telos tis diadromis! ");
                        }
                    }
                    if (coordinateIsOnRoute) break;
                }
                if(!coordinateIsOnRoute){
                    logger.info("###############");
                    logger.info("ALERT found. Alert count so far is " + ++alertCounter);
                }
            }
        }
    }
}