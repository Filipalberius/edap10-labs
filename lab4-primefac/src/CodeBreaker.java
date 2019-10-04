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

    private ExecutorService threadPool = Executors.newFixedThreadPool(2);


    // -----------------------------------------------------------------------
    
    private CodeBreaker() {
        StatusWindow w  = new StatusWindow();
        w.enableErrorChecks();

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

        WorklistItem workListItem = new WorklistItem(n, message);
        ProgressItem progressItem = new ProgressItem(n, message);

        SwingUtilities.invokeLater(() -> workList.add(workListItem));

        ProgressTracker tracker = new ProgressTracker() {
            private int totalProgress = 0;

            @Override
            public void onProgress(int ppmDelta) {
                totalProgress += ppmDelta;
                SwingUtilities.invokeLater(() -> {
                    progressItem.getProgressBar().setValue(totalProgress);
                    mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
                });
            }
        };

        workListItem.getBreakButton().addActionListener(e -> {
            SwingUtilities.invokeLater(() ->{
                progressList.add(progressItem);
                workList.remove(workListItem);
                mainProgressBar.setMaximum(mainProgressBar.getMaximum() + 1000000);
            });

            Runnable crack = () -> {
                String plainText = Factorizer.crack(message, n, tracker);
                SwingUtilities.invokeLater(() -> {
                    progressItem.getTextArea().setText(plainText);
                    progressItem.addRemoveButton();
                });
            };

            threadPool.submit(crack);
        });

        progressItem.getRemoveButton().addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                progressList.remove(progressItem);
                mainProgressBar.setValue(mainProgressBar.getValue() - 1000000);
                mainProgressBar.setMaximum(mainProgressBar.getMaximum() - 1000000);
            });
        });
    }
}
