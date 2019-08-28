/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.so1.test;

/**
 *
 * @author mmendez
 */
public class ProducerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        try {
//            String nombreCola = "queue.so1.demo";
//            String nombreServicio = "EjemploCola";
//            String serverLocation = "failover:(tcp://localhost:61616)?timeout=3000";
//
//            String message = " {"
//                    + " \"platformOrigin\":\"" + "1" + "\","
//                    + " \"platformDestiny\":\"" + "2" + "\""
//                    + "}";
//            try {
//                QueueUtil.send(nombreCola, true, true, 0, nombreServicio, message, serverLocation);
//
//            } catch (Exception e) {
//                System.out.println("Error....");
//                e.printStackTrace();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        TestProducerThread hilo1 = new TestProducerThread();
        TestProducerThread hilo2 = new TestProducerThread();
        TestProducerThread hilo3 = new TestProducerThread();
        TestProducerThread hilo4 = new TestProducerThread();
        TestProducerThread hilo5 = new TestProducerThread();

        hilo1.start();
        hilo2.start();
        hilo3.start();
        hilo4.start();
        hilo5.start();

    }

}
