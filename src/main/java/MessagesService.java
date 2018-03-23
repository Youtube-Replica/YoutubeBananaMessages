import Commands.Command;
import Commands.delete.DeleteMessage;
import Commands.get.GetMessage;
import Commands.get.GetMessages;
import Commands.patch.UpdateMessage;
import Commands.post.CreateMessage;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class MessagesService {
    private static final String RPC_QUEUE_NAME = "messages-request";

    public static void main(String [] argv) {

        //initialize thread pool of fixed size
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setHost("localhost");
        factory.setUsername("rabbitmq");
        factory.setPassword("rabbitmq");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(100);

            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    System.out.println("Responding to corrID: "+ properties.getCorrelationId());

                    try {
                        String message = new String(body, "UTF-8");
                        JSONParser parser = new JSONParser();
                        JSONObject messageBody = (JSONObject) parser.parse(message);
                        String command = (String) messageBody.get("command");
                        Command cmd = null;
                        switch (command) {
                            case "CreateMessages":   cmd = new CreateMessage();
                                break;
                            case "RetrieveMessages":
                                cmd = new GetMessage();
                                HashMap<String, Object> paramsHashMap  =
                                        cmd.jsonToMap((JSONObject) messageBody.get("parameters"));
                                if (paramsHashMap.containsKey("user_id")) {
                                    cmd = new GetMessages();
                                }
                                break;
                            case "UpdateMessages":   cmd = new UpdateMessage();
                                break;
                            case "DeleteMessages":   cmd = new DeleteMessage();
                                break;
                        }


                        HashMap<String, Object> props = new HashMap<String, Object>();
                        props.put("channel", channel);
                        props.put("properties", properties);
                        props.put("replyProps", replyProps);
                        props.put("envelope", envelope);
                        props.put("body", message);

                        cmd.init(props);
                        executor.submit(cmd);
                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
