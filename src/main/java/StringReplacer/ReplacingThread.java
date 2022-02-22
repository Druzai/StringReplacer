package StringReplacer;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ReplacingThread extends SwingWorker<Object, String> {
    private final Config config;
    private final JFrame mainWindow;
    private final JButton jButtonStart;

    public ReplacingThread(Config config, JFrame mainWindow, JButton jButton) {
        this.config = config;
        this.mainWindow = mainWindow;
        this.jButtonStart = jButton;
    }

    @Override
    protected String doInBackground() throws IOException {
        List<Path> files = Files.find(Paths.get(config.getPathToFolder()), 999,
                (p, bfa) -> bfa.isRegularFile()).collect(Collectors.toList());
        for (Path file : files) {
            try (FileReader fileReader = new FileReader(file.toFile())) {
                int i;
                StringBuilder stringBuilder = new StringBuilder();
                while ((i = fileReader.read()) != -1)
                    stringBuilder.append((char) i);
                String origStr = stringBuilder.toString();
                String str = origStr.replace(config.getReplaceString(), config.getReplaceToString());
                if (origStr.equals(str))
                    continue;

                try (FileWriter fileWriter = new FileWriter(file.toFile())) {
                    fileWriter.append(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Done!";
    }

    @Override
    protected void done() {
        JFrame jFrame = new JFrame("Done");
        jFrame.setLayout(new FlowLayout());
        jFrame.add(new Label("Files processed!"));
        jFrame.setMinimumSize(new Dimension(250, 90));
        jFrame.setLocationRelativeTo(mainWindow);
        jFrame.setResizable(false);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setVisible(true);
        jButtonStart.setEnabled(true);
    }
}
