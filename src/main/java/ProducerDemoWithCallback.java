import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProducerDemoWithCallback {


    String addr;
    int port;

    List<Topic> topics = new ArrayList<>();

    List<Value> publisherValues = new ArrayList<>();
    List<BusLine> publisherBusLines = new ArrayList<>();
    List<RouteCode> publisherRouteCodes = new ArrayList<>();
    List<BusPosition> publisherBusPositions = new ArrayList<>();
    HashSet<Topic> topicHashSet = new HashSet<>();
    HashSet<String> lineCodeHashSet = new HashSet<>();

    public ProducerDemoWithCallback(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public static void main(String[] args) {

        //find all the vehicle ids in one set

        HashSet<Integer> vehicleIds = findAllVehicleIdsSet();

        for(int vehicleId : vehicleIds) {

            //create the Kafka Producer

            ProducerDemoWithCallback producerDemoWithCallback = new ProducerDemoWithCallback(null,0);
            producerDemoWithCallback.initiateTopicAndValueList(vehicleId);

            final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);

            String bootstrapServers = "127.0.0.1:9092";

            //create Producer properties
            Properties properties = new Properties();

            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            //create the Kafka Producer

            KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

            for(Value value : producerDemoWithCallback.publisherValues) {
                Topic topic = new Topic(value.getBuslineId());
                Message message = new Message(topic, value);

                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic.getBusLineInput(), message.value.toString());

                //send data - asynchronous
                producer.send(record, new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        //executes every time a record is successfully sent or an exception is thrown
                        if (e == null) {
                            //the record was successfully sent
                            logger.info("Received new metadata: \n" +
                                    "Topic:" + recordMetadata.topic() + "\n" +
                                    "Partition:" + recordMetadata.partition() + "\n" +
                                    "Offset:" + recordMetadata.offset() + "\n" +
                                    "Timestamp:" + recordMetadata.timestamp());
                        } else {
                            logger.error("Error while producing", e);
                        }
                    }
                });

            }

            //flush data
            producer.flush();
            //close and flush producer
            producer.close();
        }

    }

    private static HashSet<Integer> findAllVehicleIdsSet() {

        HashSet<Integer> vehicleIds = new HashSet<>();
        String busPositionsFile = "./Dataset/DS_project_dataset/busPositionsNew.txt";
        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(busPositionsFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                int vehicleId = Integer.parseInt(fields[2]);
                return vehicleId; })
                    .forEach(line -> vehicleIds.add(line));

        } catch(IOException e) {
            e.printStackTrace();
        }
        return vehicleIds;
    }

    private void initiateTopicAndValueList(int vehicleId) {

        // Me vasi to vehicle id vres ola ta bus position objects apo to arxeio me ta bus positions
        findFromBusPositionsFile(vehicleId);

        // Vres ola ta distinct route codes apo ta parapanw bus position objects
        List<String> distinctRouteCodes = publisherBusPositions.stream().map(BusPosition::getRouteCode).distinct().collect(Collectors.toList());

        // Apo ta distinct route codes Strings vres ola ta route code objects apo to arxeio me ta route codes
        findFromRouteCodesFile(distinctRouteCodes);

        // Vres ola ta distinct line codes apo ta parapanw route code objects
        List<String> distinctLineCodes = publisherRouteCodes.stream().map(RouteCode::getLineCode).distinct().collect(Collectors.toList());

        // Apo ta distinct bus lines Strings vres ola ta bus line objects apo to arxeio me ta bus lines
        findFromBusLinesFile(distinctLineCodes);

        findValueFromBusPositionsList();
        for (BusLine busline : publisherBusLines) {
            populateAllNullValues(busline);
        }

        System.out.println(publisherValues.toString());

        System.out.print("Vehicle with id " + vehicleId + " is responsible for the following lines/topics: ");
        for(BusLine busLine: publisherBusLines) {
            topics.add(new Topic(busLine.getLineId()));
            System.out.print(busLine.getLineId() + " ");
        }
        System.out.println();
    }

    private void findFromBusPositionsFile(int vehicleId) {

        String busPositionsFile = "./Dataset/DS_project_dataset/busPositionsNew.txt";
        //String busPositionsFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busPositionsNew.txt";

        // read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(busPositionsFile))) {

            stream.map(line -> {
                String[] fields = line.split(",");
                BusPosition busPosition = new BusPosition(fields[0], fields[1], fields[2], Double.parseDouble(fields[3]), Double.parseDouble(fields[4]), fields[5]);
                return busPosition; })
                    .filter(busPositionline -> busPositionline.getVehicleId().equals(String.valueOf(vehicleId)))
                    .forEach(busPositionline -> publisherBusPositions.add(busPositionline));

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void findFromRouteCodesFile(List<String> routeCodes) {

        String routeCodesFile = "./Dataset/DS_project_dataset/RouteCodesNew.txt";
        //String routeCodesFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\RouteCodesNew.txt";

        //read file into stream, try-with-resources
        for(String routeCode : routeCodes) {
            try (Stream<String> stream = Files.lines(Paths.get(routeCodesFile))) {

                stream.map(line -> {
                    String[] fields = line.split(",");
                    RouteCode routeCodeObject = new RouteCode(fields[1], fields[0], fields[3]);
                    return routeCodeObject; })
                        .filter(routeCodeline -> routeCodeline.getRouteCode().equals(String.valueOf(routeCode)))
                        .forEach(routeCodeline -> publisherRouteCodes.add(routeCodeline));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void findFromBusLinesFile(List<String> busLines) {

        String busLinesFile = "./Dataset/DS_project_dataset/busLinesNew.txt";
        //String busLinesFile = "C:\\Users\\nikos\\workspace\\aueb\\distributed systems\\ds-project-2019\\Dataset\\DS_project_dataset\\busLinesNew.txt";

        //read file into stream, try-with-resources
        for (String busLine : busLines) {
            try (Stream<String> stream = Files.lines(Paths.get(busLinesFile))) {

                stream.map(line -> {
                    String[] fields = line.split(",");
                    BusLine myBusLine = new BusLine(fields[0], fields[1], fields[2]);
                    return myBusLine; })
                        .filter(busLineline -> busLineline.getLineCode().equals(String.valueOf(busLine)))
                        .forEach(busLineline -> publisherBusLines.add(busLineline));

            } catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    private void findValueFromBusPositionsList() {
        for(BusPosition busPosition : publisherBusPositions){ // tha mporouse na ginei kai me stream.map() se java 8
            publisherValues.add(new Value(busPosition.getLineCode(), busPosition.getRouteCode(), busPosition.getVehicleId()
                    , null, null, busPosition.getTimestampOfBusPosition(), busPosition.getLatitude(), busPosition.getLongitude()));
        }
    }

    private void populateAllNullValues(BusLine busline) {
        for (Value value : publisherValues) {
            if (value.getLineNumber().equals(busline.getLineCode())) {
                value.setBuslineId(busline.getLineId());
                value.setLineName(busline.getDescriptionEnglish());
            }
        }
    }
}
