/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.so1.test;

import com.so1.queue.QueueUtil;

/**
 *
 * @author mmendez
 */
public class TestProducerThread extends Thread {
    
    @Override
    public void run() {
        
        while (true) {
            String nombreCola = "queue.so1.demo";
            String nombreServicio = "EjemploCola";
            String serverLocation = "failover:(tcp://172.17.0.2:61616)?timeout=3000";
            
            String message = " {"
                    + " \"id\":" + "1" + ","
                    + " \"nombre\":\"" + "manuel" + "\","
                    + " \"edad\":" + "2" 
                    + "}";
            try {
                QueueUtil.send(nombreCola, true, true, 0, nombreServicio, message, serverLocation);
                
                System.out.println("Enviando mensaje....");
                //Thread.sleep(100);
                
            } catch (Exception e) {
                System.out.println("Error....");
                e.printStackTrace();
            }
        }
    }
    
}
