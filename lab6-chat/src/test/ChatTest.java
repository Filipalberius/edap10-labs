package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chat.client.ChatLog;
import chat.client.ServerControl;
import chat.client.Twit;

class ChatTest {

    @BeforeEach
    void setUp() throws Exception {
        ServerControl.restartServer();
    }

    @Test
    void testOneTwit() throws InterruptedException {
        final int NBR_MESSAGES  = 5;     // number of messages from each client
        final int MESSAGE_DELAY = 100;   // maximal delay between messages 

        Twit t = new Twit("twit", NBR_MESSAGES, MESSAGE_DELAY);
        t.start();
        t.join();

        ChatLog.expect(1, NBR_MESSAGES);
    }

    @Test
    void testTwoTwits() throws InterruptedException {
        final int NBR_TWITS     = 2;     // number of clients
        final int NBR_MESSAGES  = 5;     // number of messages from each client
        final int MESSAGE_DELAY = 100;   // maximal delay between messages 

        Twit[] twits = new Twit[NBR_TWITS];
        for (int i = 0; i < twits.length; i++) {
            twits[i] = new Twit("twit" + (i + 1), NBR_MESSAGES, MESSAGE_DELAY);
        }
        for (Twit t : twits) {
            t.start();
        }
        for (Twit t : twits) {
            t.join();
        }

        ChatLog.expect(NBR_TWITS, NBR_TWITS * NBR_MESSAGES);
    }

    @Test
    void test100Twits() throws InterruptedException{
        final int NBR_TWITS     = 100;     // number of clients
        final int NBR_MESSAGES  = 300;     // number of messages from each client
        final int MESSAGE_DELAY = 0;   // maximal delay between messages

        Twit[] twits = new Twit[NBR_TWITS];
        for (int i = 0; i < twits.length; i++) {
            twits[i] = new Twit("twit" + (i + 1), NBR_MESSAGES, MESSAGE_DELAY);
        }
        for (Twit t : twits) {
            t.start();
        }
        for (Twit t : twits) {
            t.join();
        }

        ChatLog.expect(NBR_TWITS, NBR_TWITS * NBR_MESSAGES);
    }
}
