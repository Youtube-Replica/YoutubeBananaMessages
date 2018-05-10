package Commands.get;

import Commands.Command;
import Model.Message;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;


public class GetMessage extends Command {
    public void execute() {
        HashMap<String, Object> props = parameters;
        Channel               channel = (Channel) props.get("channel");
        JSONParser            parser = new JSONParser();

        try {
            JSONObject messageBody = (JSONObject) parser.parse((String) props.get("body"));
            HashMap<String, Object> paramsHashMap  = jsonToMap((JSONObject) messageBody.get("parameters"));
            AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
            AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
            Envelope envelope               = (Envelope) props.get("envelope");
            HashMap<String, Object> message = Message.get((String) paramsHashMap.get("id"));
            JSONObject response = jsonFromMap(message);
            channel.basicPublish(
                    "",
                    properties.getReplyTo(),
                    replyProps,
                    response.toString().getBytes("UTF-8"));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
