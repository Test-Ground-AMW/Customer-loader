package lk.ijse.dep11.app.wscontroller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.CustomerTO;
import lk.ijse.dep11.app.to.RequestTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

public class CustomerWSController extends TextWebSocketHandler {

    @Autowired
    private LocalValidatorFactoryBean validatorFactory;
    @Autowired
    private Environment env;
    @Autowired
    private ObjectMapper mapper;
    private HikariDataSource pool;

    @PostConstruct
    public void initialize(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
        config.setUsername(env.getRequiredProperty("spring.datasource.username"));
        config.setPassword(env.getRequiredProperty("spring.datasource.password"));
        config.setDriverClassName(env.getRequiredProperty("spring.datasource.driver-class-name"));
        config.setMaximumPoolSize(env.getRequiredProperty("spring.datasource.hikari.maximum-pool-size", Integer.class));
        pool = new HikariDataSource(config);
    }

    @PreDestroy
    public void destroy(){
        pool.close();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            RequestTO messageObj = mapper.readValue(message.getPayload(), RequestTO.class);
            if (validatorFactory.getValidator().validate(messageObj).isEmpty()){
                try (Connection connection = pool.getConnection()) {
                    PreparedStatement stm = connection
                            .prepareStatement("SELECT * FROM customer WHERE id LIKE ? OR" +
                                    " first_name LIKE ? OR last_name LIKE ? OR contact LIKE ? OR" +
                                    " country LIKE ? ORDER BY id LIMIT ? OFFSET ?");
                    for (int i = 1; i <= 5; i++) stm.setObject(i, "%" + messageObj.getQuery() + "%");
                    stm.setInt(6, messageObj.getSize());
                    stm.setInt(7, (messageObj.getPage() - 1) * messageObj.getSize());
                    ResultSet rst = stm.executeQuery();
                    List<CustomerTO> customerList = new LinkedList<>();
                    while (rst.next()){
                        int id = rst.getInt("id");
                        String firstName = rst.getString("first_name");
                        String lastName = rst.getString("last_name");
                        String contact = rst.getString("contact");
                        String country = rst.getString("country");
                        customerList.add(new CustomerTO(id, firstName, lastName, contact, country));
                    }
                    String jsonCustomerList = mapper.writeValueAsString(customerList);
                    session.sendMessage(new TextMessage(jsonCustomerList));
                }
            }else{
                session.sendMessage(new TextMessage("Invalid Request"));
            }
        }catch (JacksonException exp){
            session.sendMessage(new TextMessage("Invalid JSON"));
        }
}}
