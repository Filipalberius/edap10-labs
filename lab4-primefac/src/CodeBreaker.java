import java.awt.*;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

    private final JPanel workList;
    private final JPanel progressList;
    
    private final JProgressBar mainProgressBar;

    ExecutorService threadPool = Executors.newFixedThreadPool(2);


    // -----------------------------------------------------------------------
    
    private CodeBreaker() {
        StatusWindow w  = new StatusWindow();

        workList        = w.getWorkList();
        progressList    = w.getProgressList();
        mainProgressBar = w.getProgressBar();

        new Sniffer(this).start();
    }
    
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) throws Exception {

        SwingUtilities.invokeLater(CodeBreaker::new);
    }

    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {
        System.out.println("message intercepted (N=" + n + ")...");
        JButton breakButton = new JButton("Break");

        WorklistItem workListItem = new WorklistItem(n, message, breakButton);
        ProgressItem progressItem = new ProgressItem(n, message);

        ProgressTracker tracker = new ProgressTracker() {
            private int totalProgress = 0;

            @Override
            public void onProgress(int ppmDelta) {
                totalProgress += ppmDelta;
                progressItem.getProgressBar().setValue(totalProgress);
            }
        };

        breakButton.addActionListener(e -> {
            progressList.add(progressItem);
            workList.remove(workListItem);

            threadPool.submit(() -> {
                Factorizer.crack(message, n, tracker);
            });
        });

        SwingUtilities.invokeLater(() -> workList.add(workListItem));
    }
}
