package com.so1.queue;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.pool.PooledConnectionFactory;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.DeliveryMode;
import javax.jms.Destination;

public class QueueUtil {

    static final Map<String, PooledConnectionFactory> connPoolCache = Collections.synchronizedMap(new WeakHashMap<String, PooledConnectionFactory>());
    private final static Object locker = null;
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Obtiene un nuevo consumer de activeMQ
     *
     * @param serverUrl direccion de servidor
     * @param queueName nombre de la cola
     * @param serviceName nombre del servicio que lo solicita
     * @return
     * @throws QueueException
     */
    public static MessageConsumer getMessageConsumer(String serverUrl, String queueName, String serviceName) throws Exception {
        try {
            System.out.println(serverUrl + " - " + queueName + " - " + serviceName);
            Connection connection = getConnectionFromPool(serverUrl);
            if (connection.getClientID() == null) {
                String clienId = String.format("%s-%s", serviceName, UUID.randomUUID());
                connection.setClientID(clienId);
            }
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQQueue mQueue = new ActiveMQQueue(queueName);
            MessageConsumer consumer = session.createConsumer(mQueue);
            return consumer;
        } catch (JMSException e) {
            e.printStackTrace();
            throw new Exception(String.format("Error obteniendo MessageConsumer de activeMQ para los parametros: %s:<%s>/%s", serverUrl, queueName, serviceName), e);
        }
    }

    static Connection getConnectionFromPool(String serverUrl) throws Exception {
        PooledConnectionFactory factory = null;
        try {
            boolean found = true;
            if (!connPoolCache.containsKey(serverUrl)) {
//                 synchronized (locker){
                try {
                    lock.lock();
                    if (!connPoolCache.containsKey(serverUrl)) {
                        found = false;
                    }
//                 }
                } catch (Exception e) {
                } finally {
                    lock.unlock();
                }
            }

            if (found) {
                factory = connPoolCache.get(serverUrl);
            } else {
//                synchronized (locker){
                try {
                    lock.lock();
                    ConnectionFactory jmsConnectionFactory = new ActiveMQConnectionFactory(serverUrl);
                    factory = new PooledConnectionFactory();
                    factory.setConnectionFactory(jmsConnectionFactory);
                    factory.setMaxConnections(200);  	//TO DO: get from a propertyFile
                    factory.setIdleTimeout(1000 * 1000);  //TO DO:  get from a propertyFile

                    connPoolCache.put(serverUrl, factory);
                } catch (Exception e) {
                } finally {
                    lock.unlock();
                }
//                }
            }
            return factory.createConnection();
        } catch (JMSException e) {
            throw new Exception("Error obteniendo conexion del pool", e);
        }
    }

    public static Connection GetConnection(PooledConnectionFactory pooledConnectionFactory) throws JMSException {
        Connection connection = pooledConnectionFactory.createConnection();
        connection.start();
        return connection;
    }

    /**
     *
     * @param connection
     * @return
     * @throws JMSException
     */
    public static Session GetQueueSession(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;

    }

    /**
     *
     * @param session
     * @param queueName
     * @return
     * @throws JMSException
     */
    public static MessageProducer GetMessageProducer(Session session, String queueName)
            throws JMSException {
        Destination destination;
        destination = session.createQueue(queueName);

        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    public static void send(String queueName, boolean persisted, boolean transacted, int priority, String serviceName, String textMess, String serverUrl) {

        //Connection connection = pooledConnectionFactory.createConnection();
        try {
            Connection connection = getConnectionFromPool(serverUrl);
            if (connection.getClientID() == null) {
                String clienId = String.format("%s-%s", serviceName, UUID.randomUUID());
                connection.setClientID(clienId);
            }
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQQueue mQueue = new ActiveMQQueue(queueName);

            MessageProducer producer = session.createProducer(mQueue);
            producer.setDeliveryMode(persisted ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
            producer.setPriority(priority);

            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(textMess);

            //logger.debug(String.format("Sending message: priority[%d] message: %s",message.getJMSPriority(), message.getText()));
            producer.send(message);

            producer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
